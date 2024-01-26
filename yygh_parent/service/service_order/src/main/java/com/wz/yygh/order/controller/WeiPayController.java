package com.wz.yygh.order.controller;

import com.wz.yygh.common.result.R;
import com.wz.yygh.enums.PaymentTypeEnum;
import com.wz.yygh.order.service.PaymentService;
import com.wz.yygh.order.service.WeiPayService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin/")
public class WeiPayController {

    @Autowired
    private WeiPayService weiPayService;

    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "根据订单号下单，生成支付链接")
    @GetMapping("createNative/{orderId}")
    public R createNative(@PathVariable Long orderId) {
        Map<String, Object> map = weiPayService.createNative(orderId);
        return R.ok().data(map);
    }


    @ApiOperation(value = "根据订单id查询支付状态")
    @GetMapping("queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable Long orderId) {
        Map<String, String> resultMap = weiPayService.queryPayStatus(orderId);
        if (resultMap == null) {
            return R.error().message("查询出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//表示支付成功
            //更改订单表的订单状态(order_status)
            //更新支付记录表的支付状态(payment_status)
            String outTradeNo = resultMap.get("out_trade_no");
            //是微信支付，所以支付类型选择微信支付
            paymentService.paySuccess(outTradeNo, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return R.ok().message("支付成功");
        }
        return R.ok().message("支付中");//没有失败，但是没有支付成功，就还在支付中
    }
}
