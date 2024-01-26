package com.wz.yygh.hosp.controller.admin;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.DepartmentService;
import com.wz.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//与医院科室相关的请求
@RestController
@RequestMapping("/admin/hosp/department/")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //根据医院编号，查询医院所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@PathVariable("hoscode") String hoscode) {
        //因为希望树形结构展示科室信息,因此我们封装的对象必须含有children属性,那么就需要DepartmentVo
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list", list);
    }
}
