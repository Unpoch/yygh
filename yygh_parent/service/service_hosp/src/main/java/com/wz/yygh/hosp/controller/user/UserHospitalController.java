package com.wz.yygh.hosp.controller.user;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.HospitalService;
import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

//用户系统
@RestController
@RequestMapping("/user/hosp/hospital/")
public class UserHospitalController {

    @Autowired
    private HospitalService hospitalService;

    //首页展示
    @ApiOperation(value = "获取医院列表")
    @GetMapping("{page}/{limit}")
    public R getHospitalList(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            HospitalQueryVo queryVo) {
        //前端的用户系统展示所有医院信息是不分页的，但是我们可以重用这个分页接口获取所有医院数据
        Page<Hospital> pages = hospitalService.selectPage(page, limit, queryVo);
        return R.ok().data("pages", pages);//返回列表数据即可
    }

    //模糊查询
    @ApiOperation(value = "根据医院名字获取医院列表")
    @GetMapping("findByHosname/{hosname}")
    public R findByHosname(
            @ApiParam(name = "hosname", value = "医院名称", required = true)
            @PathVariable String hosname) {
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return R.ok().data("list", list);
    }

    //查看医院预约挂号详情
    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("{hoscode}")
    public R getHospitalDetail(@ApiParam(name = "hoscode", value = "医院code", required = true)
                               @PathVariable String hoscode) {
        Map<String, Object> map = hospitalService.getHospitalDetail(hoscode);
        return R.ok().data(map);
    }



}
