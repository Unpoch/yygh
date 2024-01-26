package com.wz.yygh.order.service;

import com.wz.yygh.model.order.PaymentInfo;
import com.wz.yygh.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}