package com.yxc.trade.mq;


import com.yxc.trade.domain.enums.OrderStatus;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.service.OrderDetailService;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RocketMQMessageListener(topic = "order_cancel_topic", consumerGroup = "${rocketmq.consumer.group}", consumeMode = ConsumeMode.ORDERLY)
public class OrderConsumerService implements RocketMQListener<Order> {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private RedissonClient redissonClient;

    private static final String ORDER_LOCK_PREFIX = "lock:order:";

    @Override
    public void onMessage(Order order) {
        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + order.getOrderId());
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                Order saveOrder = orderService.getById(order.getOrderId());
                if(saveOrder == null || saveOrder.getStatus() != OrderStatus.PAID) {
                    return;
                }

                saveOrder.setStatus(OrderStatus.CANCELLED);
                saveOrder.setActualEnd(new Date());
                orderService.updateById(saveOrder);
                log.info("订单：{} 超时，已自动取消", order.getOrderId());

                OrderDetail detail = new OrderDetail();
                detail.setOrderId(order.getOrderId());
                detail.setStatus(OrderStatus.CANCELLED);
                detail.setDescription("订单已超时，自动取消");
                orderDetailService.save(detail);
            }
        } catch (Exception e) {
            log.info("取消超时发生异常，订单号：{}，\n 异常信息：{}", order.getOrderId(), e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
