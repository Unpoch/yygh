package com.wz.yygh.hosp.controller.admin;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.HospitalService;
import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Api(tags = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital/")
// @CrossOrigin
public class HospitalController {


    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "带条件查询的分页")
    @GetMapping("{pageNo}/{limit}")
    public R index(@PathVariable Integer pageNo, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pages = hospitalService.selectPage(pageNo, limit, hospitalQueryVo);
        return R.ok().data("pages", pages);
    }

    @ApiOperation(value = "更新上限状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable("id") String id,
            @ApiParam(name = "status", value = "状态(0：未上线 1：已上线)")
            @PathVariable("status") Integer status) {
        hospitalService.updateStatus(id, status);
        return R.ok();
    }

    @ApiOperation(value = "查看医院详情")
    @GetMapping("show/{id}")
    public R showDetails(
            @ApiParam(name = "id", value = "医院id", required = true)
            @PathVariable("id") String id) {
        return R.ok().data("hospital", hospitalService.show(id));
    }


}
