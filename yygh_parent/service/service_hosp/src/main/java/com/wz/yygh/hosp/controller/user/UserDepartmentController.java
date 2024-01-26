package com.wz.yygh.hosp.controller.user;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.DepartmentService;
import com.wz.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//用户系统
@RestController
@RequestMapping("/user/hosp/department/")
public class UserDepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //医院预约挂号详情页面 下边所展示的所有科室列表信息
    @ApiOperation("查询医院所有科室信息")
    @GetMapping("{hoscode}")
    public R getDepartmentList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list", list);
    }
}
