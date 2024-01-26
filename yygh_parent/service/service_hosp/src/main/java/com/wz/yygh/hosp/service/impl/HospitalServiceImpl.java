package com.wz.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.cmn.client.DictFeignClient;
import com.wz.yygh.enums.DictEnum;
import com.wz.yygh.hosp.repository.HospitalRepository;
import com.wz.yygh.hosp.service.HospitalService;
import com.wz.yygh.model.hosp.Hospital;
import com.wz.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    /*
    保存医院信息
    第一次做添加操作
    以后再上传做更新操作

    注意：传过来的数据paramMap是没有：
    状态status的
    创建时间(createdTime),更新时间(updateTime),isDeleted属性
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        // String data = JSONObject.toJSONString(paramMap);//map转化成JSON字符串
        Hospital hospital = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Hospital.class);//JSON字符串转化为对象
        //hoscode是唯一的
        String hoscode = hospital.getHoscode();
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        if (null == hospitalByHoscode) {//没有加入过,做添加操作
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        } else {//存在医院信息,做修改
            hospital.setStatus(hospitalByHoscode.getStatus());//和原来保持一致
            hospital.setCreateTime(hospitalByHoscode.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(hospitalByHoscode.getStatus());//和原来保持一致
            hospital.setId(hospitalByHoscode.getId()); //根据id更新
            hospitalRepository.save(hospital);
        }
    }

    //根据医院编码查询医院信息
    @Override
    public Hospital getHospitalByHoscode(String hoscode) {
        return hospitalRepository.getHospitalByHoscode(hoscode);
    }

    /*
    医院信息分页查询,该方法是用于我们平台管理员系统
    上进行医院信息的展示的
     */
    @Override
    public Page<Hospital> selectPage(Integer pageNo, Integer limit, HospitalQueryVo hospitalQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(pageNo - 1, limit, sort);//0为第一页
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        Example<Hospital> example = Example.of(hospital, matcher);//设置查询条件
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);
        //医院Hospital中只有省市区编号,以及医院等级(数字),我们要去字典表查询对应的文字信息
        //一般一个服务对应一个表，我们最好不要在该微服务中直接操作Dict表
        //因此我们在service_cmn微服务中提供两个接口(根据省市区编号查询对应文字、根据医院等级查询对应文字),
        //然后我们在这里远程调用(OpenFeign)这个接口进行查询
        //使用流来遍历
        pages.getContent().stream().forEach(hops -> {
            this.packHospital(hops);//封装数据,编码 -> 文字信息
        });
        return pages;
    }

    //更新医院状态(上下线)
    @Override
    public void updateStatus(String id, Integer status) {
        //更新本质根据id更新,我们先根据id查询Hospital对象
        //设置id和status属性，然后调用save方法
        if (status.intValue() == 0 || status.intValue() == 1) {
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    //获取医院详情
    @Override
    public Map<String, Object> show(String id) {
        Map<String, Object> result = new HashMap<>();
        //查询出来的有code,要进行封装
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        //医院基本信息（包含医院等级）
        result.put("hospital", hospital);
        //单独处理更直观
        result.put("bookingRule", hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return result;
    }

    //根据医院名称获取医院列表
    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    //医院预约挂号详情页面
    @Override
    public Map<String, Object> getHospitalDetail(String hoscode) {
        Map<String, Object> result = new HashMap<>();
        //医院详情
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        this.packHospital(hospital);//将编号转化为对应的文字信息
        //医院规则
        result.put("hospital", hospital);
        result.put("bookingRule", hospital.getBookingRule());
        hospital.setBookingRule(null);//不需要重复返回
        return result;
    }

    /*
     * 封装数据
     */
    private Hospital packHospital(Hospital hospital) {
        String hostypeString = dictFeignClient.getNameByParentCodeAndValue(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());
        String provinceString = dictFeignClient.getNameByValue(hospital.getProvinceCode());
        String cityString = dictFeignClient.getNameByValue(hospital.getCityCode());
        String districtString = dictFeignClient.getNameByValue(hospital.getDistrictCode());
        //使用Hospital的 param属性(map，用来存放临时数据)
        hospital.getParam().put("hostypeString", hostypeString);
        //地址直接拼接即可
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }
}
