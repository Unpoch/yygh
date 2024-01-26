package com.wz.yygh.order.service;

import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import com.wz.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author wz
 * @since 2023-12-04
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrder(String scheduleId, Long patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    OrderInfo getOrderInfoById(Long orderId);

    Boolean cancelOrder(Long orderId);

    void patientRemind();

    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
