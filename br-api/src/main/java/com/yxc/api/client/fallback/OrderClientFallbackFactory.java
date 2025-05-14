package com.yxc.api.client.fallback;

import com.yxc.api.client.OrderClient;
import com.yxc.api.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {
    @Override
    public OrderClient create(Throwable cause) {
        return new OrderClient() {
            @Override
            public List<Order> getOrderByItemId(Long id) {
                log.error("根据商品ID查询订单信息失败", cause);
                return Collections.emptyList();
            }
        };
    }
}
