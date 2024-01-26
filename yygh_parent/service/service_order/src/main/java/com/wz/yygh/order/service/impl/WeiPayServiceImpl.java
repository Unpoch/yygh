package com.wz.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.enums.PaymentTypeEnum;
import com.wz.yygh.enums.RefundStatusEnum;
import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.model.order.PaymentInfo;
import com.wz.yygh.model.order.RefundInfo;
import com.wz.yygh.order.prop.WeiPayProperties;
import com.wz.yygh.order.service.OrderInfoService;
import com.wz.yygh.order.service.PaymentService;
import com.wz.yygh.order.service.RefundInfoService;
import com.wz.yygh.order.service.WeiPayService;
import com.wz.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeiPayServiceImpl implements WeiPayService {


    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WeiPayProperties weiPayProperties;

    @Autowired
    private RefundInfoService refundInfoService;

    //根据订单号下单，生成支付链接(获取微信支付的地址 url返回给前端)
    @Override
    public Map<String, Object> createNative(Long orderId) {
        try {
            //1.根据订单id获取订单信息
            OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
            //2.保存支付记录信息 -> payment_info
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //3.请求微信服务器，获取微信支付的url地址
            //1)封装参数map
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", weiPayProperties.getAppid());
            paramMap.put("mch_id", weiPayProperties.getPartner());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            Date reserveDate = orderInfo.getReserveDate();
            String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
            String body = reserveDateString + "就诊" + orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://yygh.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");

            //2)HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client设置参数
            // String xml = WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey());
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey()));
            client.setHttps(true);
            client.post();
            //3)返回第三方的数据
            String xml = client.getContent();//请求微信服务器后微信服务器返回的xml字符结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);//转换成map
            //4.将url地址返回给前端，前端利用这个地址生成二维码
            //封装结果信息
            Map<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));//这就是我们需要的url地址
            // if(null != resultMap.get("result_code")) {
            //     //微信支付二维码2小时过期，可采取2小时未支付取消订单
            //     redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            // }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    //根据订单号请求微信服务器 查询支付状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            OrderInfo orderInfo = orderInfoService.getOrderInfoById(orderId);
            //1、封装参数
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", weiPayProperties.getAppid());
            paramMap.put("mch_id", weiPayProperties.getPartner());
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());//商户的订单号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey()));
            httpClient.setHttps(true);
            httpClient.post();
            //3、返回第三方(我们)的数据，转成Map
            String xml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //4.返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    //退款
    @Override
    public boolean refund(Long orderId) {
        //先根据订单id查询支付记录，支付记录中有包含了 退款记录的信息，因此我们使用支付记录PaymentInfo
        //可以达到保存退款记录的功能
        PaymentInfo paymentInfo = paymentService.getOne(new QueryWrapper<PaymentInfo>().eq("order_id", orderId));
        //获取保存的退款记录
        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
        //这种业务场景就是 saveRefundInfo方法中提到的 用户多开取消预约页面，上一个页面提交了取消预约，这个页面又提交一次(显然后面的提交告诉它退款已成功即可)
        if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus()) {//表示已退款
            return true;
        }
        //执行微信退款
        Map<String, String> paramMap = new HashMap<>(8);
        paramMap.put("appid", weiPayProperties.getAppid());       //公众账号ID
        paramMap.put("mch_id", weiPayProperties.getPartner());   //商户编号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
        //       paramMap.put("total_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+""); 正常的设置
        //       paramMap.put("refund_fee",paymentInfo.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1"); //为了测试这样设置
        paramMap.put("refund_fee", "1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, weiPayProperties.getPartnerkey());//将参数map转换为 xml格式的参数
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            httpClient.setCert(true);//设置对证书的支持
            httpClient.setCertPassword(weiPayProperties.getPartner());//设置证书密码
            httpClient.post();

            String xml = httpClient.getContent();//微信服务器返回的响应结果
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //退款成功,更新退款记录表 的字段
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setTradeNo(resultMap.get("refund_id")); //微信退款交易号
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());//设置状态已退款
                refundInfo.setCallbackTime(new Date());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);   //更新
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
