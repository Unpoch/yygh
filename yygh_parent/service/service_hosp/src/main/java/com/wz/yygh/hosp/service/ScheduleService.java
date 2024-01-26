package com.wz.yygh.hosp.service;

import com.wz.yygh.model.hosp.Schedule;
import com.wz.yygh.vo.hosp.ScheduleOrderVo;
import com.wz.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> selectPage(int pageNo, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getRuleSchedule(long pageNo, long limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer pageNo, Integer limit, String hoscode, String depcode);

    Schedule getByScheduleId(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //修改排班
    void update(Schedule schedule);

    Schedule getByHosScheduleId(String hosScheduleId);
}
