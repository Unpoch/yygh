package com.wz.yygh.hosp.repository;

import com.wz.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
持久化层,操作mongodb,即第三方医院的数据保存在mongodb数据库中
 */
public interface HospitalRepository extends MongoRepository<Hospital, String> {

    //通过医院编码查询医院信息
    Hospital getHospitalByHoscode(String hoscode);

    //医院名称模糊查询
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
