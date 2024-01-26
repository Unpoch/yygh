package com.wz.yygh.user.service;

import com.wz.yygh.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author wz
 * @since 2023-12-01
 */
public interface PatientService extends IService<Patient> {

    List<Patient> findAllByUserId(Long userId);

    Patient getPatientById(Long id);
}
