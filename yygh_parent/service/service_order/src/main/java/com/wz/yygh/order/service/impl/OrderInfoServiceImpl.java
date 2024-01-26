package com.wz.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wz.yygh.common.exception.YyghException;
import com.wz.yygh.enums.OrderStatusEnum;
import com.wz.yygh.enums.PaymentStatusEnum;
import com.wz.yygh.hosp.client.ScheduleFeignClient;
import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.model.order.PaymentInfo;
import com.wz.yygh.model.user.Patient;
import com.wz.yygh.mq.MqConst;
import com.wz.yygh.mq.RabbitService;
import com.wz.yygh.order.mapper.OrderInfoMapper;
import com.wz.yygh.order.service.OrderInfoService;
import com.wz.yygh.order.service.PaymentService;
import com.wz.yygh.order.service.WeiPayService;
import com.wz.yygh.order.utils.HttpRequestHelper;
import com.wz.yygh.user.client.PatientFeignClient;
import com.wz.yygh.vo.hosp.ScheduleOrderVo;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import com.wz.yygh.vo.order.OrderCountVo;
import com.wz.yygh.vo.order.OrderMqVo;
import com.wz.yygh.vo.order.OrderQueryVo;
import com.wz.yygh.vo.sms.SmsVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author wz
 * @since 2023-12-04
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private ScheduleFeignClient scheduleFeignClient;

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeiPayService weiPayService;

    @Autowired
    private PaymentService paymentService;

    //取消预约
    @Override
    public Boolean cancelOrder(Long orderId) {
        //1.确定当前取消预约的时间 和 挂号订单取消预约截止时间 对比：
        //确认当前时间是否已经超过了 挂号订单规定的取消预约的截止时间
        //如果超过了，直接抛出异常，不让用户取消
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {//超过
            throw new YyghException(20001, "超过退号截止时间");
        }
        //2.立即请求第三方医院，通知 该用户已取消预约
        //    2.1 第三方医院不同意取消，抛出异常，不能取消
        HashMap<String, Object> hospitalParamMap = new HashMap<>();
        hospitalParamMap.put("hoscode", orderInfo.getHoscode());
        hospitalParamMap.put("hosRecordId", orderInfo.getHosRecordId());
        hospitalParamMap.put("timestamp", HttpRequestHelper.getTimestamp());
        hospitalParamMap.put("sign", "");
        JSONObject result = HttpRequestHelper.sendRequest(hospitalParamMap, "http://localhost:9998/order/updateCancelStatus");
        if (result == null || result.getIntValue("code") != 200) {//表示失败
            throw new YyghException(20001, "取消预约失败");
        }
        //3.判断用户是否对当前挂号订单是否已支付(挂号成功且未支付可以取消，挂号成功且支付成功也可以取消预约)
        //    3.1 如果已支付，【退款】，继续往下执行
        //    3.2 如果未支付，继续往下执行，走共有的业务逻辑
        if (orderInfo.getOrderStatus() == OrderStatusEnum.PAID.getStatus()) {//已支付要退款
            boolean isRefund = weiPayService.refund(orderId);
            if (!isRefund) {//退款不成功
                throw new YyghException(20001, "退款失败");
            }
        }
        //4.更新订单的订单状态 和 支付记录表的支付状态 -> 已取消
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        baseMapper.updateById(orderInfo);
        //更新支付记录状态。
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.REFUND.getStatus()); //退款
        paymentService.updateById(paymentInfo);
        //5.更新医生的剩余可预约数信息( + 1) -> rabbitmq
        //6.给就诊人发送短信提示：取消预约了...
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());
        //短信提示
        SmsVo smsVo = new SmsVo();
        smsVo.setPhone(orderInfo.getPatientPhone());
        orderMqVo.setSmsVo(smsVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        return true;
    }

    //就医提醒
    @Override
    public void patientRemind() {
        //将预约时间是今天的用户都查出来，给他们发送就医提醒
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        //正常来说这里是查出 订单状态不是 -1的
        queryWrapper.ne("order_status", OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfos = baseMapper.selectList(queryWrapper);
        for (OrderInfo orderInfo : orderInfos) {
            SmsVo smsVo = new SmsVo();
            smsVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            smsVo.setParam(param);
            //发送短信提示
            System.out.println("要发送短信了哈");
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SMS, MqConst.ROUTING_SMS_ITEM, smsVo);
        }
    }


    //生成订单
    //本质就是往order_info这个表保存数据
    //order_info表的数据由三部分构成：排班信息(包含医院医生部门等信息),就诊人信息,和第三方医院传过来的挂号信息
    //1.根据scheduleId获取排班信息 (远程调用service_hosp -> 根据排班id获取排班信息)
    //2.根据patientId获取就诊人信息 (远程调用service_user -> 根据就诊人id获取就诊人信息)
    //3.给第三方医院发送请求，获取第三方医院返回的数据，确认当前用户能否挂号
    //1) 如果不能挂号，直接抛出异常
    //2) 如果可以挂号，将三部分信息插入order_info表
    //  4.更新平台上对应医生的剩余可预约数(借助rabbitmq)
    //  5.给就诊人发送短信提醒(远程调用service_sms -> 给就诊人发送短信)
    //  6.返回订单的id
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //注意这个scheduleId是hosScheduleId不是 主键
        ScheduleOrderVo scheduleOrderVo = scheduleFeignClient.getScheduleOrderVo(scheduleId);
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        if (new DateTime(scheduleOrderVo.getStopTime()).isBeforeNow()) {//此时此刻在当天挂号截止时间(不能预约今天的了)
            throw new YyghException(20001, "超过挂号截止时间");
        }
        //第三方医院接口需要的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", scheduleOrderVo.getReserveDate());
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount());
        JSONObject result = HttpRequestHelper.sendRequest(paramMap,
                "http://localhost:9998/order/submitOrder");
        if (result != null && result.getInteger("code") == 200) {
            JSONObject data = result.getJSONObject("data");//将data这个键对应的值 解析成JSON对象
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = data.getString("hosRecordId");
            //预约序号
            Integer number = data.getInteger("number");
            //取号时间
            String fetchTime = data.getString("fetchTime");
            //取号地址
            String fetchAddress = data.getString("fetchAddress");
            //添加数据到订单表order_info
            OrderInfo orderInfo = new OrderInfo();
            //设置添加数据--排班数据
            BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
            //设置添加数据--就诊人数据
            //订单号
            String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
            orderInfo.setUserId(patient.getUserId());
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());

            //设置添加数据--医院接口返回数据
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);

            //往订单表插入数据
            baseMapper.insert(orderInfo);//mybatis-plus支持 表中插入数据时将表的主键赋值给POJO对象的id属性
            //根据医院返回数据，更新排班数量
            /*
            使用rabbitmq的场景：1.分布式系统 2.异步通信
            如果使用openFeign远程调用service_hosp更新，是可以实现的
            但是 service_order要等到 service_hosp返回远程调用的结果后，才会继续进行下面的业务逻辑:发短信
            这样相当于是同步的，而且效率会慢些

            正常来说，更新对应医生可预约数 和 发送短信两个业务是可以异步进行的
            service_order发送消息给mq1，告诉它id和available，让它更新可预约数(service_hosp什么时候处理这条消息我们不管)
            service_order发送消息给mq2，让它发送短信 ， service_sms作为消费者消费这条消息，发送短信信息
             */

            //排班可预约数
            Integer reservedNumber = data.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = data.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setScheduleId(scheduleId);//更新哪个排班的 号源
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);

            //短信提示
            SmsVo smsVo = new SmsVo();
            smsVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime() == 0 ? "上午" : "下午");
            Map<String, Object> param = new HashMap<String, Object>() {{
                put("title", orderInfo.getHosname() + "|" + orderInfo.getDepname() + "|" + orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            smsVo.setParam(param);

            orderMqVo.setSmsVo(smsVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,
                    MqConst.ROUTING_ORDER, orderMqVo);//生产消息
            //7 返回订单号
            return orderInfo.getId();
        } else {
            System.out.println("下单失败");
            throw new YyghException(20001, "挂号失败");
        }
    }

    //查询用户订单信息，分页展示
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        Long userId = orderQueryVo.getUserId();//用户id
        String outTradeNo = orderQueryVo.getOutTradeNo();//订单号
        String hostname = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();//下订单时间
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();//下订单时间
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        //TODO: 判断当前用户id非空，使用用户id查询  !!!
        if (!StringUtils.isEmpty(userId)) {
            wrapper.eq("user_id", userId);
        }
        if (!StringUtils.isEmpty(outTradeNo)) {
            wrapper.eq("out_trade_no", outTradeNo);
        }
        if (!StringUtils.isEmpty(hostname)) {
            wrapper.like("hosname", hostname);//模糊查询
        }
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);//大于等于
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);//大于等于起始时间
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);//小于等于结束时间
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    //根据订单id获取订单信息
    @Override
    public OrderInfo getOrderInfoById(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    //远程调用接口，获取预约统计信息
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        //OrderCountVo中封装着预约统计需要的信息：预约日期 和 预约日期当天对应的人数
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);
        // List<String> dateList = orderCountVoList.stream().map(item -> item.getReserveDate()).collect(Collectors.toList());简写方式如下
        List<String> dateList = orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList = orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("dateList",dateList);
        map.put("countList",countList);
        return map;
    }


    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
