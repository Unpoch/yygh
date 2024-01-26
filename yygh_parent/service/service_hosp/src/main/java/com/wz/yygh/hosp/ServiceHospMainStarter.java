package com.wz.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "com.wz") //手动指定扫描包，这样该模块所依赖的模块 com.wz包下的也能被扫描到
@MapperScan(value = "com.wz.yygh.hosp.mapper") //扫描mapper接口
@EnableDiscoveryClient //nacos注册中心
@EnableFeignClients(basePackages = "com.wz") //必须要指定包,跟@ComponentScan是一样的,该模块所依赖的模块,com.wz包下的也能被找到
public class ServiceHospMainStarter {

    public static void main(String[] args) {
        SpringApplication.run(ServiceHospMainStarter.class, args);
    }
}
