package com.wz.yygh.mq;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    //配置消息转换器
    //生产者端发送POJO对象：把发送的rabbitmq中的POJO对象 自动转换成JSON格式存储
    //消费者端接收POJO对象：将JSON格式的数据 自动转换成POJO对象
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
