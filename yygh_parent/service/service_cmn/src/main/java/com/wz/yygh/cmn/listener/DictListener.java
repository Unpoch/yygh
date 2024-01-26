package com.wz.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.wz.yygh.cmn.mapper.DictMapper;
import com.wz.yygh.model.cmn.Dict;
import com.wz.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//上传Excel文件需要的Listener
@Component
public class DictListener extends AnalysisEventListener<DictEeVo> {

    @Autowired
    private DictMapper dictMapper;

    //一行一行读取
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //将DictEeVo转化为DictVo，方便往数据库插入数据
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<Dict>();
        queryWrapper.eq("id", dict.getId());
        Integer count = dictMapper.selectCount(queryWrapper);
        if (count > 0) {
            dictMapper.updateById(dict);
        } else {
            dictMapper.insert(dict);
        }
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
