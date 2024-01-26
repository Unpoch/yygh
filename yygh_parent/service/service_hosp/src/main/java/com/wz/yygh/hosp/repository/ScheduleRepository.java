package com.wz.yygh.hosp.repository;

import com.wz.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    //根据hoscode和hosScheduleId查询排班信息
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);

    Schedule getScheduleByHosScheduleId(String hosScheduleId);
}
