package com.wz.yygh.order.service;

import java.util.Map;

public interface WeiPayService {
    Map<String,Object> createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    boolean refund(Long orderId);
}
