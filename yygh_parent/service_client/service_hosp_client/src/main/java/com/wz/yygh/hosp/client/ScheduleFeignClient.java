package com.wz.yygh.hosp.client;

import com.wz.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp") //指定被调用方在注册中心的应用名
public interface ScheduleFeignClient {

    @GetMapping("/user/hosp/schedule/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);
}
