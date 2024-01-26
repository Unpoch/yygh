package com.wz.yygh.oss.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aliyun.oss.file")
//1.@PropertySource不支持yml文件 2.@PropertySource不能能主启动类上的@EnableConfigurationProperties一起使用
@PropertySource(value = {"classpath:oss.properties"}) //加载配置文件
@Data
@Component
public class OssProperties {

    private String endpoint;
    private String keyId;
    private String keySecret;
    private String bucketName;

}
