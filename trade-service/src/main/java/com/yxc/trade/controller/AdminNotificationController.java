package com.yxc.trade.controller;

import com.yxc.trade.domain.po.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminNotificationController {

    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final String DESTINATION = "/topic/admin/notifications";

    // 发送新订单通知
    public void sendNewOrderNotification(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_ORDER");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION, payload);
    }

    // 发送取消订单通知
    public void sendCancelOrderNotification(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "CANCEL_ORDER");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION, payload);
    }

    // 发送申请退款通知
    public void sendRefundOrderNotification(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "REFUND_ORDER");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION, payload);
    }

    // 发送归还电池通知
    public void sendReturnBatteryNotification(Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "RETURN_BATTERY");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION, payload);
    }

}