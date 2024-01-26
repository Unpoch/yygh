package com.wz.yygh.statistics.service;

import com.wz.yygh.vo.order.OrderCountQueryVo;

import java.util.Map;

public interface StatisticsService {
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
