package com.yxc.trade.controller;

import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.dto.*;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.vo.CreateOrderVO;
import com.yxc.trade.domain.vo.OrderDetailVO;
import com.yxc.trade.domain.vo.OutTimeOrderInfoVO;
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

    @GetMapping("/pageQuery")
    private Result<PageVO<Order>> pageQuery(OrderPageQueryDTO pageQuery) {
        return orderService.pageQuery(pageQuery);
    }

    @GetMapping("pageQueryByUserId")
    private Result<PageVO<Order>> pageQueryByUserId(OrderPageQueryDTO pageQuery) {
        return orderService.pageQueryByUserId(pageQuery);
    }

    @GetMapping("getOrderDetail/{orderId}")
    private Result<OrderDetailVO> getOrderDetail(@PathVariable(value = "orderId") Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @PostMapping("/createOrder")
    private Result<CreateOrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        return orderService.createOrder(createOrderDTO);
    }

    @PutMapping("/pay")
    private Result<?> pay(@RequestBody PayOrderDTO payOrderDTO) {
        return orderService.payOrder(payOrderDTO);
    }

    @PutMapping("/ship")
    private Result<?> shipOrder(@RequestBody ShipOrderDTO shipOrderDTO) {
        return orderService.shipOrder(shipOrderDTO);
    }

    @PutMapping("/cancel")
    private Result<?> cancelOrder(@RequestBody CancelOrderDTO cancelOrderDTO) {
        return orderService.cancelOrder(cancelOrderDTO);
    }

    @PutMapping("/userConfirm")
    private Result<Long> confirm(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.confirmOrder(confirmOrderDTO);
    }

    @PutMapping("/renewRent")
    private Result<Long> renewRent(@RequestBody RenewRentDTO renewRentDTO) {
        return orderService.renewRent(renewRentDTO);
    }

    @PutMapping("/returnBattery")
    private Result<Long> returnBattery(@RequestBody ReturnBatteryDTO returnBatteryDTO) {
        return orderService.returnBattery(returnBatteryDTO);
    }

    @PutMapping("/applyRefund")
    private Result<Long> applyRefund(@RequestBody ApplyRefundDTO applyRefundDTO) {
        return orderService.applyRefund(applyRefundDTO);
    }

    @PutMapping("/returnBatteryConfirm")
    private Result<Long> adminConfirm(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.returnBatteryConfirm(confirmOrderDTO);
    }

    @PutMapping("/agreeRefund")
    private Result<Long> agreeRefund(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.agreeRefund(confirmOrderDTO);
    }

    @PutMapping("/disagreeRefund")
    private Result<Long> disagreeRefund(@RequestBody DisagreeRefundDTO disagreeRefundDTO) {
        return orderService.disagreeRefund(disagreeRefundDTO);
    }

    @GetMapping("/getTimeoutOrderInfo")
    private Result<OutTimeOrderInfoVO> getTimeoutOrderInfo(@RequestParam("orderId") Long orderId) {
        return orderService.getTimeoutOrderInfo(orderId);
    }

    @PutMapping("/payTimeoutOrder")
    private Result<Long> payTimeoutOrder(@RequestBody PayTimeoutOrderDTO payTimeoutOrderDTO) {
        return orderService.payTimeoutOrder(payTimeoutOrderDTO);
    }

}
