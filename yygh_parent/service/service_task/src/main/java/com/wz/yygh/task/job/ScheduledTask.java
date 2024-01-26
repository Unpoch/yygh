package com.wz.yygh.task.job;

import com.wz.yygh.mq.MqConst;
import com.wz.yygh.mq.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 定时任务
 */
@Component
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    //定时任务1：给就诊人发送就医短信提醒
    //cron表达式: 秒 分 时 日 月 星期 年[最高到2099](可以省略)
    // * 表示任意, ?表示无所谓,   - 表示连续的时间段, /n:表示每隔n 时间执行
    // ,:隔开没有规律的时间
    // 日(每月第几天) 和 星期  之中必须有一个是 ?
    // @Scheduled(cron = "0/20 * * * * ?") //测试，每隔20s执行一次
    @Scheduled(cron = "0 0 6 * * ?") //每天早上6点发送
    public void patientRemindTask() {
        System.out.println(new Date().toLocaleString());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
