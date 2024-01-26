package com.wz.yygh.order.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "weipay") //决定了读取properties文件中以谁开头的配置项
@PropertySource(value = "classpath:weipay.properties") //加载配置文件
@Component
@Data
public class WeiPayProperties {

    private String appid;
    private String partner;
    private String partnerkey;
    private String cert;
}
