package com.wz.yygh.user.service.impl;

import com.wz.yygh.cmn.client.DictFeignClient;
import com.wz.yygh.enums.DictEnum;
import com.wz.yygh.model.user.Patient;
import com.wz.yygh.user.mapper.PatientMapper;
import com.wz.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 就诊人表 服务实现类
 * </p>
 *
 * @author wz
 * @since 2023-12-01
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    //根据user_id查询 所有对应就诊人信息
    @Override
    public List<Patient> findAllByUserId(Long userId) {
        List<Patient> patients = baseMapper.selectList(new QueryWrapper<Patient>().eq("user_id", userId));
        //还需要封装数据(编码 -> 文字信息)
        patients.stream().forEach(item -> {
            this.packagePatient(item);
        });
        return patients;
    }

    //根据id查询就诊人信息
    @Override
    public Patient getPatientById(Long id) {
        return this.packagePatient(baseMapper.selectById(id));//要进行其他参数的封装
    }

    //Patient对象里面其他参数封装
    private Patient packagePatient(Patient patient) {
        //根据certificatesType查询对应文字,就需要远程调用DictService中的方法,那么需要注入DictFeignClient
        //根据证件类型编码获取证件类型
        String certificatesTypeString =
                dictFeignClient.getNameByParentCodeAndValue(DictEnum.CERTIFICATES_TYPE.getDictCode()
                        , patient.getCertificatesType());
        //联系人证件类型
        // String contactsCertificatesTypeString =
        //         dictFeignClient.getNameByParentCodeAndValue(DictEnum.CERTIFICATES_TYPE.getDictCode()
        //                 , patient.getContactsCertificatesType());
        //省
        String provinceString = dictFeignClient.getNameByValue(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getNameByValue(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getNameByValue(patient.getDistrictCode());
        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        // patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        //全地址
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }
}
