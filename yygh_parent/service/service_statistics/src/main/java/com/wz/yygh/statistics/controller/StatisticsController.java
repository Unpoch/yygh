package com.wz.yygh.statistics.controller;

import com.wz.yygh.common.result.R;
import com.wz.yygh.statistics.service.StatisticsService;
import com.wz.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics/")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public R getCountMap(@ApiParam(name = "orderCountQueryVo", value = "查询对象", required = false)
                         OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = statisticsService.getCountMap(orderCountQueryVo);
        return R.ok().data(map);
    }

}
