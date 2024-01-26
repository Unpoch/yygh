package com.wz.yygh.hosp.repository;

import com.wz.yygh.hosp.pojo.Actor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/*
该类用于测试练习MongoRepository,并非项目接口
 */
//自定义持久化层接口
public interface ActorRepository extends MongoRepository<Actor, String> {

    /*
    自定义查询方法
     */
    //模糊查询
    public List<Actor> findByActorNameLike(String name);
}
