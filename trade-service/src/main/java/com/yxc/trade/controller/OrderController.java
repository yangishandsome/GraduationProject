package com.yxc.trade.controller;

import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.api.po.OrderDetail;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private ItemClient itemClient;

    @GetMapping("/test")
    private Result<Long> test() {
        orderService.test();
        return Result.ok(1L);
    }
}
