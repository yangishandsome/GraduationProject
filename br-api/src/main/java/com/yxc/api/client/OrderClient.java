package com.yxc.api.client;

import com.yxc.api.client.fallback.OrderClientFallbackFactory;
import com.yxc.api.config.DefaultFeignConfig;
import com.yxc.api.po.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trade-service",
        url = "https://localhost:8082",
        configuration = DefaultFeignConfig.class,
        fallbackFactory = OrderClientFallbackFactory.class)
public interface OrderClient {
    @GetMapping("/order/getOrderByItemId")
    List<Order> getOrderByItemId(@RequestParam("id") Long id);
}
