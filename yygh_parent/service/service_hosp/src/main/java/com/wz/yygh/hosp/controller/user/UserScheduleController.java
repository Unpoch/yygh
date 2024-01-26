package com.wz.yygh.hosp.controller.user;

import com.wz.yygh.common.result.R;
import com.wz.yygh.hosp.service.ScheduleService;
import com.wz.yygh.model.hosp.Schedule;
import com.wz.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/hosp/schedule/")
public class UserScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //不建议接口复用，建议方法复用

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{pageNo}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(
            @PathVariable Integer pageNo,
            @PathVariable Integer limit,
            @PathVariable String hoscode,
            @PathVariable String depcode) {
        Map<String, Object> map = scheduleService.getBookingScheduleRule(pageNo, limit, hoscode, depcode);
        return R.ok().data(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {
        List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return R.ok().data("scheduleList", scheduleList);
    }

    //根据排班id获取 排班信息
    @ApiOperation("获取排班详情")
    @GetMapping("getSchedule/{scheduleId}")
    public R getSchedule(@PathVariable String scheduleId) {
        Schedule schedule = scheduleService.getByScheduleId(scheduleId);
        return R.ok().data("schedule", schedule);
    }

    /**
    提供远程调用的方法，根据排班id获取排班信息
     */
    @ApiOperation("根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@ApiParam(name = "scheduleId", value = "排班id", required = true)
                                              @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }
}
