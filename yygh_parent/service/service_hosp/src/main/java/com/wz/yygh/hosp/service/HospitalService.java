package com.wz.yygh.hosp.service;

import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getHospitalByHoscode(String hoscode);

    Page<Hospital> selectPage(Integer pageNo, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String,Object> show(String id);

    List<Hospital> findByHosname(String hosname);

    Map<String, Object> getHospitalDetail(String hoscode);
}
