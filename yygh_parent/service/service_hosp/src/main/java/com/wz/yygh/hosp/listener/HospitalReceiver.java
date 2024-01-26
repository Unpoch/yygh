package com.wz.yygh.hosp.listener;

import com.wz.yygh.hosp.service.ScheduleService;
import com.wz.yygh.model.hosp.Schedule;
import com.wz.yygh.mq.MqConst;
import com.wz.yygh.mq.RabbitService;
import com.wz.yygh.vo.order.OrderMqVo;
import com.wz.yygh.vo.sms.SmsVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),//创建队列
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),//创建交换机
            key = {MqConst.ROUTING_ORDER}
    ))
    //确认挂号： 可预约数 由第三方医院传的数据决定(orderMqVo封装了availableNumber)
    //取消预约： 自己平台的可预约数 + 1(orderMqVo没有封装availableNumber)
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        //orderMqVo.getScheduleId() 是 hosScheduleId 不是 主键
        Schedule schedule = scheduleService.getByHosScheduleId(orderMqVo.getScheduleId());
        if (null != orderMqVo.getAvailableNumber()) {
            //更新排班
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        } else {
            //取消预约更新预约数
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
        }
        scheduleService.update(schedule);
        //发送短信
        SmsVo smsVo = orderMqVo.getSmsVo();
        if (smsVo != null) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS, MqConst.ROUTING_SMS_ITEM, smsVo);
        }
    }
}
