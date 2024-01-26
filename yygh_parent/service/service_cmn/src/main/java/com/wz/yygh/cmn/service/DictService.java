package com.wz.yygh.cmn.service;

import com.wz.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author wz
 * @since 2023-11-15
 */
public interface DictService extends IService<Dict> {

    List<Dict> getChildrenListByPid(Long id);

    void exportData(HttpServletResponse response);

    void importDictData(MultipartFile file);

    //根据上级编码与值获取数据字典名称
    String getNameByParentDictCodeAndValue(String parentDictCode, String value);


    List<Dict> findByDictCode(String dictCode);
}
