package com.wz.yygh.statistics.service.impl;

import com.wz.yygh.order.client.OrderInfoFeignClient;
import com.wz.yygh.statistics.service.StatisticsService;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;

    //获取预约统计信息
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        return orderInfoFeignClient.getCountMap(orderCountQueryVo);
    }
}
