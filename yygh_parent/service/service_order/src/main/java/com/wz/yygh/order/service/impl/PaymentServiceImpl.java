package com.wz.yygh.order.service.impl;

import com.wz.yygh.enums.OrderStatusEnum;
import com.wz.yygh.enums.PaymentStatusEnum;
import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.model.order.PaymentInfo;
import com.wz.yygh.order.mapper.PaymentMapper;
import com.wz.yygh.order.service.OrderInfoService;
import com.wz.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderInfoService orderInfoService;

    //往支付记录表中保存信息
    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        //用户在点击'支付'时，就会就会保存一条支付记录，但若用户没有支付
        //关掉生成的二维码页面，再次点击'支付'，又会请求后端，生成一条针对同一订单的支付记录
        //因此，为了避免这种情况，现根据订单id和支付类型去payment_info中查询是否有对应的支付记录
        //如果已经存在，就不需要再保存了，直接返回
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", order.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0) return;
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTotalAmount(order.getAmount());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());//状态是未支付
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd") + "|" + order.getHosname() + "|" + order.getDepname() + "|" + order.getTitle();
        paymentInfo.setSubject(subject);
        //还有三个字段：trade_no, callback_time,callback_content 是支付完成后回来填充的
        baseMapper.insert(paymentInfo);
    }

    //更新订单状态和更新支付记录状态
    @Transactional //要么全部成功，要么失败
    @Override
    public void paySuccess(String outTradeNo, Integer weixtype, Map<String, String> resultMap) {
        //更改订单表的订单状态(order_status)
        QueryWrapper<OrderInfo> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("out_trade_no", outTradeNo);//根据订单交易号查询订单信息
        OrderInfo orderInfo = orderInfoService.getOne(orderWrapper);
        //状态改为已支付
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        //更新支付记录表的支付状态(payment_status)
        QueryWrapper<PaymentInfo> paymentWrapper = new QueryWrapper<>();
        paymentWrapper.eq("out_trade_no", outTradeNo);//根据订单交易号查询支付信息
        PaymentInfo paymentInfo = this.getOne(paymentWrapper);
        //设置状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));//这个交易编号 是微信服务器生成的 微信支付订单编号
        paymentInfo.setCallbackContent(resultMap.toString());
        paymentInfo.setCallbackTime(new Date());
        baseMapper.updateById(paymentInfo);
    }
}