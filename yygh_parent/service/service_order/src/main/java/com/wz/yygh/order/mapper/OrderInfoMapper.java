package com.wz.yygh.order.mapper;

import com.wz.yygh.model.order.OrderInfo;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import com.wz.yygh.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author wz
 * @since 2023-12-04
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //获取预约统计信息
    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}
