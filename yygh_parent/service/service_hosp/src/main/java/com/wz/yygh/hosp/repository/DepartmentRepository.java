package com.wz.yygh.hosp.repository;

import com.wz.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

//持久化层,操作mongodb
public interface DepartmentRepository extends MongoRepository<Department, String> {

    //根据医院编码 和 科室编码 查询科室信息
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);

}
