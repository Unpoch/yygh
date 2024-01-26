package com.wz.yygh.order.service.impl;

import com.wz.yygh.enums.RefundStatusEnum;
import com.wz.yygh.model.order.PaymentInfo;
import com.wz.yygh.model.order.RefundInfo;
import com.wz.yygh.order.mapper.RefundInfoMapper;
import com.wz.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {


    //保存退款记录
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //注意：针对每一个支付记录 仅有一条退款记录
        //因此要判断 退款记录表中是否已经存在对应的 退款记录，存在直接返回(会用用户多开取消预约页面，可能多次取消)
        QueryWrapper<RefundInfo> refundWrapper = new QueryWrapper<>();
        refundWrapper.eq("order_id", paymentInfo.getOrderId());
        refundWrapper.eq("payment_type", paymentInfo.getPaymentType());
        RefundInfo refundInfo = baseMapper.selectOne(refundWrapper);
        if(refundInfo != null) {//之前已经保存过 对应的退款记录了
            return refundInfo;
        }
        //保存支付记录
        refundInfo = new RefundInfo();
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());//退款中
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}