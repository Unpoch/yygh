package com.wz.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 数据字典API接口
 */
@FeignClient(value = "service-cmn") //指定被调用方在注册中心的名称
public interface DictFeignClient {


    @GetMapping("/admin/cmn/dict/getName/{parentDictCode}/{value}")
    public String getNameByParentCodeAndValue(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value);


    @GetMapping("/admin/cmn/dict/getName/{value}")
    public String getNameByValue(
            @PathVariable("value") String value);
}
