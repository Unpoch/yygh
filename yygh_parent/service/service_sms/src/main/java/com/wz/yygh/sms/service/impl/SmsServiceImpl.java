package com.wz.yygh.sms.service.impl;

import com.wz.yygh.sms.service.SmsService;
import com.wz.yygh.sms.utils.HttpUtils;
import com.wz.yygh.sms.utils.RandomUtil;
import com.wz.yygh.vo.sms.SmsVo;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SmsServiceImpl implements SmsService {


    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    //发送验证码
    @Override
    public boolean send(String phone) {
        String queryCode = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(queryCode)) return true;
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "2101346f71774767845fae736f0567c3";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<>();
        String code = RandomUtil.getFourBitRandom();//生成验证码
        querys.put("mobile", phone);
        querys.put("param", "验证码:" + code + "，有效时间五分钟！");
        Map<String, String> bodys = new HashMap<>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
            //将验证码在redis中保存一份
            redisTemplate.opsForValue().set(phone, code, 20, TimeUnit.DAYS);
            System.out.println("code = " + code);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //发送就诊人挂号 短信
    @Override
    public boolean sendMessage(SmsVo smsVo) {
        //阿里云发送短信提示....现在不行了
        System.out.println("发送短信");
        if (!StringUtils.isEmpty(smsVo.getPhone())) {
            //String code = (String)msmVo.getParam().get("code");
            //仅为了测试
            String code = RandomUtil.getFourBitRandom();
            System.out.println("给就诊人发送短信成功");
            return this.send(smsVo.getPhone());
        }
        return false;
    }

}
