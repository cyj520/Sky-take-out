package com.sky.task;

/**
 * @author: ChuYangjie
 * @date: 2024/3/27 18:22
 * @version: 1.0
 */

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

///**
// * 自定义定时任务类
// */
//@Component
//@Slf4j
//public class OrderTask {
//    @Autowired
//    private OrderMapper orderMapper;
//
//    @Scheduled(cron = "0 * * * * ?")// 每分钟触发一次
//    public void processTimeoutOrder() {
//        log.info("检查超时订单，{}", LocalDateTime.now());
//        // 查找订单状态为待支付，且下单时间在15分钟之前的订单
//        List<Orders> ordersList =
//                orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
//
//        if (ordersList != null && ordersList.size() != 0) {
//            ordersList.forEach(orders -> {
//                orders.setStatus(Orders.CANCELLED);
//                orders.setCancelReason("订单超时，自动取消");
//                orders.setCancelTime(LocalDateTime.now());
//                orderMapper.update(orders);
//            });
//        }
//    }
//
//    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1:00:00执行一次
//    public void processDeliveryOrder() {
//        log.info("检查配送中订单");
//
//        List<Orders> ordersList =
//                orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
//        if (ordersList != null && ordersList.size() != 0) {
//            ordersList.forEach(orders -> {
//                orders.setStatus(Orders.COMPLETED);
//                orderMapper.update(orders);
//            });
//        }
//    }
//}
