package com.wz.yygh.hosp.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


/**
 * 该类用于测试Mongodb
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("actor") //对应mongodb中test库中集合 actor
// @Document("bbb")  //取mongodb的test库中找 bbb集合
public class Actor {

    //该属性和mongodb的_id对应,如果想要对应，默认情况下名字必须是id
    //加上注解以后,表明该属性和mongodb中的_id(主键)是对应的，此时属性名是可以自定义的
    @Id
    private String id;
    // private String aaaid;

    //其他属性和mongodb中的字段保持一致
    // @Field(value = "actor_name") //集合内的字段叫actor_name,@Field 建立映射关系,不指定该注解，名字默认一致
    private String actorName;
    private boolean gender;
    private Date birth;
}
