package com.wz.yygh.hosp.controller.admin;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.ScheduleService;
import com.wz.yygh.model.hosp.Schedule;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule/")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //根据医院编码和科室编码查询排班规则数据
    @ApiOperation(value = "查询排班规则数据")
    @GetMapping("getScheduleRule/{pageNo}/{limit}/{hoscode}/{depcode}")
    public R getScheduleRule(@PathVariable long pageNo,
                             @PathVariable long limit,
                             @PathVariable String hoscode,
                             @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getRuleSchedule(pageNo, limit, hoscode, depcode);
        return R.ok().data(map);
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @ApiOperation(value = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail( @PathVariable String hoscode,
                                @PathVariable String depcode,
                                @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return R.ok().data("list",list);
    }
}
