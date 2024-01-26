package com.wz.yygh.user.client;

import com.wz.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user")
public interface PatientFeignClient {

    @GetMapping("/user/userinfo/patient/inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id);
}
