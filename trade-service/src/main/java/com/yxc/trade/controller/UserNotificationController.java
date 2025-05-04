package com.yxc.trade.controller;

import com.yxc.common.utils.UserContext;
import com.yxc.trade.domain.po.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserNotificationController {

    @Resource
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final String DESTINATION = "/topic/user/notifications/";

    // 发送配送单通知
    public void sendShipOrderNotification(Long userId, Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SHIP_ORDER");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION + userId, payload);
    }

    // 发送同意退款通知
    public void sendAgreeRefundNotification(Long userId, Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "AGREE_REFUND");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION + userId, payload);
    }

    // 发送拒绝退款通知
    public void sendDisagreeRefundNotification(Long userId, Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DISAGREE_REFUND");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION + userId, payload);
    }

    // 发送确认电池归还通知
    public void sendConfirmBatteryReturnNotification(Long userId, Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "BATTERY_RETURN");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION + userId, payload);
    }

    // 发送订单超时通知
    public void sendTimeoutOrderNotification(Long userId, Order order) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "TIMEOUT_ORDER");
        payload.put("data", order);
        payload.put("timestamp", System.currentTimeMillis());

        simpMessagingTemplate.convertAndSend(DESTINATION + userId, payload);
    }

}