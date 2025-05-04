package com.yxc.trade.task;

import com.yxc.trade.controller.UserNotificationController;
import com.yxc.trade.domain.enums.OrderStatus;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.service.OrderDetailService;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderTask {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderDetailService orderDetailService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserNotificationController userNotificationController;

    private static final Integer OUT_TIME_MINUTES = -1;

    private static final String ORDER_LOCK_PREFIX = "lock:order:";

    @Async("scheduleExecutor")
    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelOutTimeOrder() {
        log.info("取消超时订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(OUT_TIME_MINUTES);
        List<Order> orders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.CREATED)
                .le(Order::getCreatedAt, time)
                .list();
        List<OrderDetail> details = new ArrayList<>(orders.size());
        for(Order order : orders) {
            RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + order.getOrderId());
            try {
                if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                    order.setStatus(OrderStatus.CANCELLED);
                    orderService.lambdaUpdate()
                            .set(Order::getStatus, OrderStatus.CANCELLED)
                            .eq(Order::getOrderId, order.getOrderId())
                            .eq(Order::getStatus, OrderStatus.CREATED)
                            .update();
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(order.getOrderId());
                    detail.setStatus(OrderStatus.CANCELLED);
                    detail.setDescription("订单已超时，自动取消");
                    details.add(detail);
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
        orderDetailService.saveBatch(details);
    }

    /**
     * 每天0点检测超时租赁订单
     */
    @Async("scheduleExecutor")
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "0 */1 * * * ?")
    @Transactional
    public void detectOutTimeRent() {
        log.info("检测超时租赁订单");
        LocalDate today = LocalDate.now();
        List<Order> outTimeOrders = orderService.lambdaQuery()
                .eq(Order::getStatus, OrderStatus.RENTING)
                .lt(Order::getExpectedEnd, today)
                .list();
        List<OrderDetail> detailList = new ArrayList<>(outTimeOrders.size());
        for(Order order : outTimeOrders) {
            order.setStatus(OrderStatus.TIMEOUT);
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(order.getOrderId());
            detail.setStatus(OrderStatus.TIMEOUT);
            detail.setDescription("您的租赁已超时，请处理");
            detailList.add(detail);
            userNotificationController.sendTimeoutOrderNotification(order.getUserId(), order);
        }
        orderService.updateBatchById(outTimeOrders);
        orderDetailService.saveBatch(detailList);
    }

}
