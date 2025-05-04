package com.yxc.trade.controller;

import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.dto.*;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.vo.*;
import com.yxc.trade.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/order")
@AllArgsConstructor
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private UserNotificationController userNotificationController;

    @GetMapping("/pageQuery")
    public Result<PageVO<Order>> pageQuery(OrderPageQueryDTO pageQuery) {
        log.info("pageQuery:{}", pageQuery);
        log.info("orderService:{}", orderService);
        return orderService.pageQuery(pageQuery);
    }

    @GetMapping("/pageQueryByUserId")
    public Result<PageVO<Order>> pageQueryByUserId(OrderPageQueryDTO pageQuery) {
        return orderService.pageQueryByUserId(pageQuery);
    }

    @GetMapping("/getOrderDetail/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable(value = "orderId") Long orderId) {
        return orderService.getOrderDetail(orderId);
    }

    @GetMapping("/getCountByStatus")
    public Result<GetCountByStatusVO> getCountByStatus() {
        return orderService.getCountByStatus();
    }

    @PostMapping("/createOrder")
    public Result<CreateOrderVO> createOrder(@RequestBody CreateOrderDTO createOrderDTO) {
        return orderService.createOrder(createOrderDTO);
    }

    @PutMapping("/pay")
    public Result<?> pay(@RequestBody PayOrderDTO payOrderDTO) {
        return orderService.payOrder(payOrderDTO);
    }

    @PutMapping("/ship")
    public Result<?> shipOrder(@RequestBody ShipOrderDTO shipOrderDTO) {
        return orderService.shipOrder(shipOrderDTO);
    }

    @PutMapping("/cancel")
    public Result<?> cancelOrder(@RequestBody CancelOrderDTO cancelOrderDTO) {
        return orderService.cancelOrder(cancelOrderDTO);
    }

    @PutMapping("/userConfirm")
    public Result<Long> confirm(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.confirmOrder(confirmOrderDTO);
    }

    @PutMapping("/renewRent")
    public Result<Long> renewRent(@RequestBody RenewRentDTO renewRentDTO) {
        return orderService.renewRent(renewRentDTO);
    }

    @PutMapping("/returnBattery")
    public Result<Long> returnBattery(@RequestBody ReturnBatteryDTO returnBatteryDTO) {
        return orderService.returnBattery(returnBatteryDTO);
    }

    @PutMapping("/applyRefund")
    public Result<Long> applyRefund(@RequestBody ApplyRefundDTO applyRefundDTO) {
        return orderService.applyRefund(applyRefundDTO);
    }

    @PutMapping("/returnBatteryConfirm")
    public Result<Long> adminConfirm(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.returnBatteryConfirm(confirmOrderDTO);
    }

    @PutMapping("/agreeRefund")
    public Result<Long> agreeRefund(@RequestBody ConfirmOrderDTO confirmOrderDTO) {
        return orderService.agreeRefund(confirmOrderDTO);
    }

    @PutMapping("/disagreeRefund")
    public Result<Long> disagreeRefund(@RequestBody DisagreeRefundDTO disagreeRefundDTO) {
        return orderService.disagreeRefund(disagreeRefundDTO);
    }

    @GetMapping("/getTimeoutOrderInfo")
    public Result<OutTimeOrderInfoVO> getTimeoutOrderInfo(@RequestParam("orderId") Long orderId) {
        return orderService.getTimeoutOrderInfo(orderId);
    }

    @PutMapping("/payTimeoutOrder")
    public Result<Long> payTimeoutOrder(@RequestBody PayTimeoutOrderDTO payTimeoutOrderDTO) {
        return orderService.payTimeoutOrder(payTimeoutOrderDTO);
    }

    @GetMapping("/informHandelTimeoutOrder")
    public Result<Long> informHandelTimeoutOrder(@RequestParam("orderId") Long orderId) {
        return orderService.informHandelTimeoutOrder(orderId);
    }

    @GetMapping("/getTodayOrderData")
    public Result<TodayOrderDataVO> getTodayOrderData() {
        return orderService.getTodayOrderData();
    }

    @GetMapping("/getTurnover/today")
    public Result<GetTurnoverVO> getTodayData() {
        return orderService.getTodayData();
    }

    @GetMapping("/getTurnover/yesterday")
    public Result<GetTurnoverVO> getYesterday() {
        return orderService.getYesterday();
    }

    @GetMapping("/getTurnover/last7days")
    public Result<GetTurnoverVO> getLast7days() {
        return orderService.getLast7days();
    }

    @GetMapping("/getTurnover/last15days")
    public Result<GetTurnoverVO> getLast15days() {
        return orderService.getLast15days();
    }

    @GetMapping("/getTurnover/last30days")
    public Result<GetTurnoverVO> getLast30days() {
        return orderService.getLast30days();
    }

}
