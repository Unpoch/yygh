package com.wz.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.wz.yygh.cmn.listener.DictListener;
import com.wz.yygh.cmn.mapper.DictMapper;
import com.wz.yygh.cmn.service.DictService;
import com.wz.yygh.model.cmn.Dict;
import com.wz.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author wz
 * @since 2023-11-15
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    //注入DictListener
    @Autowired
    private DictListener dictListener;


    /*
    SpringCache
     */
    //加Redis缓存
    // 键(key) = dict::selectIndexList1 ,假设pid = 1
    // 值(value) = 方法的返回值
    // @Cacheable(value = "dict", key = "keyGenerator")
    @Cacheable(value = "dict", key = "'selectIndexList'+#pid")
    @Override
    public List<Dict> getChildrenListByPid(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);
        //查询出了所有parent_id = id的子数据之后，这些子数据还有可能子数据,
        //因此要根据查询出的子数据的hasChildren属性判断
        List<Dict> dictList = baseMapper.selectList(queryWrapper);
        for (Dict dict : dictList) {
            //拿到id，将该id作为父id再去表里查询是否有子节点,设置dict的hasChildren属性
            dict.setHasChildren(isHasChildren(dict.getId()));
        }
        return dictList;
    }

    //数据字典导出为excel
    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            //这里使用DictEeVo是因为该类的属性使用了ExcelProperty,方便直接设置导出Excel的列名
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for (Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictVo);//将DictEeVo需要的属性设置
                dictVoList.add(dictVo);
            }
            //导出为Excel
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    //导入excel中的数据相当于添加操作，要将原缓存清空，否则导致数据不一致
    @CacheEvict(value = "dict", allEntries = true) //allEntries=true表示将 dict下的子键也清空(dict::....)
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据上级编码与值获取数据字典名称
    /*
    例如：查询hosType = 1
    select * from dict where dict_code = 'Hostype'
     -> 查询出id=10000,name='医院等级',parent_id=1,dict_code='Hostype'...
    然后我们就查询谁的parent_id = 10000,就能查询医院等级，然后value=1就是查询指定的医院等级
    select * from dict where parent_id = 10000 and value = 1(这个就是hosType)
     */
    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //如果value能唯一定位数据字典，parentDictCode可以传空，例如：省市区的value值能够唯一确定
        if(StringUtils.isEmpty(parentDictCode)) {
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        } else {
            Dict parentDict = this.getDictByDictCode(parentDictCode);
            if(null == parentDict) return "";
            //将parentDict的id作为parent_id 和 value字段联合查询
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id",
                    parentDict.getId()).eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        }
        return "";
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict codeDict = this.getDictByDictCode(dictCode);
        if(null == codeDict) return null;
        return this.getChildrenListByPid(codeDict.getId());
    }

    //根据dict_code查询字典信息(Dict)
    private Dict getDictByDictCode(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        return baseMapper.selectOne(wrapper);
    }

    //根据pid查询是否有子节点
    private boolean isHasChildren(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;//能查出数据说明存在子节点
    }
}
