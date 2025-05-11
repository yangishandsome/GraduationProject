package com.yxc.trade.mq;

import com.yxc.trade.domain.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class OrderProducerService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendOrderCreateMessage(Order order) {
        String topic = "order_create_topic";
        // 发送延时消息，延迟级别为14（默认是1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h）
        int delayLevel = 14; // 延迟10分钟
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(order).build(), 3000, delayLevel);
    }

    public void sendOrderCancelMessage(Order order) {
        String topic = "order_cancel_topic";
        // 发送延时消息，延迟级别为14（默认是1s, 5s, 10s, 30s, 1m, 2m, 3m, 4m, 5m, 6m, 7m, 8m, 9m, 10m, 20m, 30m, 1h, 2h）
        int delayLevel = 14; // 延迟10分钟
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(order).build(), 3000, delayLevel);
    }
}
