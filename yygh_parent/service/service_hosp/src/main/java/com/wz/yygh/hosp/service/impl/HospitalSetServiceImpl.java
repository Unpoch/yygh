package com.wz.yygh.hosp.service.impl;

import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.hosp.mapper.HospitalSetMapper;
import com.wz.yygh.hosp.service.HospitalSetService;
import com.wz.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院设置表 服务实现类
 * </p>
 *
 * @author wz
 * @since 2023-11-08
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {


    //根据医院编码查询医院签名密钥
    @Override
    public String getSignKey(String hoscode) {
        HospitalSet hospitalSet = this.getByHoscode(hoscode);
        if (null == hospitalSet) {
            throw new YyghException(20001, "失败");
        }
        return hospitalSet.getSignKey();
    }

    //根据医院编码获取医院设置信息
    private HospitalSet getByHoscode(String hoscode) {
        return baseMapper.selectOne(new QueryWrapper<HospitalSet>().eq("hoscode", hoscode));
    }
}
