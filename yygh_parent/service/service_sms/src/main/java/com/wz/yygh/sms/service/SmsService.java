package com.wz.yygh.sms.service;

import com.wz.yygh.vo.sms.SmsVo;

public interface SmsService {
    boolean send(String PhoneNumbers);

    //发送短信接口
    boolean sendMessage(SmsVo smsVo);
}
