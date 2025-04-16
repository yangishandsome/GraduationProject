package com.yxc.trade.controller;

import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.dto.CreateOrderDTO;
import com.yxc.trade.domain.dto.PayOrderDTO;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.domain.vo.CreateOrderVO;
import com.yxc.trade.domain.vo.OrderDetailVO;
import com.yxc.trade.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/createOrder")
    private Result<CreateOrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        return orderService.createOrder(createOrderDTO);
    }

    @PutMapping("/pay")
    private Result<?> pay(@RequestBody PayOrderDTO payOrderDTO) {
        return orderService.payOrder(payOrderDTO);
    }

    @GetMapping("pageQuery")
    private Result<PageVO<Order>> pageQuery(PageQuery pageQuery) {
        return orderService.pageQuery(pageQuery);
    }

    @GetMapping("getOrderDetail/{orderId}")
    private Result<OrderDetailVO> getOrderDetail(@PathVariable(value = "orderId") Long orderId) {
        return orderService.getOrderDetail(orderId);
    }
}
