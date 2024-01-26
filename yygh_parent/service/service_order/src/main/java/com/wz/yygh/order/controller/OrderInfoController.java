package com.wz.yygh.order.controller;


import com.wz.yygh.common.result.R;
import com.wz.yygh.common.utils.AuthContextHolder;
import com.wz.yygh.enums.OrderStatusEnum;
import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.order.service.OrderInfoService;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import com.wz.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author wz
 * @since 2023-12-04
 */
@RestController
@RequestMapping("/api/order/orderInfo/")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @ApiOperation(value = "确认挂号")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(@ApiParam(name = "scheduleId", value = "排班id", required = true)
                         @PathVariable String scheduleId,
                         @ApiParam(name = "patientId", value = "就诊人id", required = true)
                         @PathVariable Long patientId) {
        //将订单id返回，当点击 确认挂号后 携带订单id跳转页面再次请求后端获取订单数据
        Long orderId = orderInfoService.saveOrder(scheduleId, patientId);
        return R.ok().data("orderId", orderId);
    }

    @ApiOperation(value = "查询当前用户的订单信息,带查询的分页")
    @GetMapping("auth/{pageNo}/{limit}")
    public R list(@PathVariable Long pageNo,
                  @PathVariable Long limit,
                  OrderQueryVo orderQueryVo,
                  HttpServletRequest request) {//通过请求头的token信息获取用户id
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(pageNo, limit);
        IPage<OrderInfo> pageModel =
                orderInfoService.selectPage(pageParam, orderQueryVo);
        return R.ok().data("pageModel", pageModel);
    }

    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public R getStatusList() {
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }

    //根据订单id查询订单详情
    @ApiOperation(value = "订单详情")
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable("orderId") Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
        return R.ok().data("orderInfo", orderInfo);
    }

    //取消预约
    @ApiOperation("取消预约")
    @GetMapping("/auth/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable("orderId") Long orderId) {
        Boolean flag = orderInfoService.cancelOrder(orderId);
        return R.ok().data("flag", flag);
    }

    /*
    远程调用接口，返回预约统计数据
    openFeign远程调用时，底层传输数据都是JSON数据，因此要使用@RequestBody注解，那么就要用@postMapping
     */
    @ApiOperation("远程调用接口，获取预约统计信息")
    @PostMapping("inner/getCountMap")     //post请求有请求体，将请求体的JSON数据转换为POJO类对象orderCountQueryVo
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo) {
        return orderInfoService.getCountMap(orderCountQueryVo);
    }
}

