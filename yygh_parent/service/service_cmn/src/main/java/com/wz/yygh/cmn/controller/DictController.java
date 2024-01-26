package com.wz.yygh.cmn.controller;


import com.wz.yygh.cmn.service.DictService;
import com.wz.yygh.common.result.R;
import com.wz.yygh.model.cmn.Dict;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * </p>
 *
 * @author wz
 * @since 2023-11-15
 */
@RestController
@RequestMapping("/admin/cmn/dict/")
// @CrossOrigin 全局已经做了配置,不需要了
public class DictController {


    @Autowired
    private DictService dictService;

    //根据数据id查询子节点列表
    @ApiOperation(value = "根据数据id查询子数据列表")
    @GetMapping("findChildData/{pid}")
    public R findChildData(@PathVariable Long pid) {
        return R.ok().data("list", dictService.getChildrenListByPid(pid));
    }

    //根据dictCode查询下层节点
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "findByDictCode/{dictCode}")
    public R findByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }

    //导出数据字典
    @ApiOperation(value = "数据字典导出为Excel文件")
    @GetMapping("exportData")
    public R exportData(HttpServletResponse httpServletResponse) {
        dictService.exportData(httpServletResponse);
        return R.ok();
    }

    //导入Excel文件
    @ApiOperation(value = "导入Excel文件")
    @PostMapping("importData")
    //注意，这个名字一定是file,因为前端有一个name属性值,没有设置默认值为file
    //那么该参数名要和前端的name属性值保持一致
    public R importData(MultipartFile file) {
        dictService.importDictData(file);
        return R.ok();
    }

    /*
    提供两个api接口，如省市区不需要上级编码，医院等级需要上级编码
     */
    //这个接口是给微服务远程调用的,因此调用方需要什么我们返回什么
    //而且注意，微服务远程调用,@PathVariable一定要指定value属性，不要省略
    @ApiOperation(value = "根据parentId和value获取数据字典名称(文字信息)")
    @GetMapping("getName/{parentDictCode}/{value}")
    public String getNameByParentCodeAndValue(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation(value = "根据医院省市区编号查询对应的文字信息")
    @GetMapping("getName/{value}")
    public String getNameByValue(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue("",value);
    }

}

