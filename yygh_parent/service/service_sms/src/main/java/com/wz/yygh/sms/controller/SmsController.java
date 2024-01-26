package com.wz.yygh.sms.controller;

import com.wz.yygh.common.result.R;
import com.wz.yygh.sms.service.SmsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/sms/")
public class SmsController {

    @Autowired
    private SmsService smsService;


    @ApiOperation("发送短信验证码")
    @GetMapping("send/{phone}")
    public R sendCode(@PathVariable String phone) {
        if (smsService.send(phone))
            return R.ok();
        return R.error().message("发送短信失败");
    }
}
