package com.yxc.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.dto.*;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.vo.CreateOrderVO;
import com.yxc.trade.domain.vo.OrderDetailVO;
import com.yxc.trade.domain.vo.OutTimeOrderInfoVO;

public interface OrderService extends IService<Order> {

    Result<CreateOrderVO> createOrder(CreateOrderDTO createOrderDTO);

    Result<?> payOrder(PayOrderDTO payOrderDTO);

    Result<PageVO<Order>> pageQuery(OrderPageQueryDTO pageQuery);

    Result<PageVO<Order>> pageQueryByUserId(OrderPageQueryDTO pageQuery);

    Result<OrderDetailVO> getOrderDetail(Long orderId);

    Result<?> shipOrder(ShipOrderDTO shipOrderDTO);

    Result<?> cancelOrder(CancelOrderDTO cancelOrderDTO);

    Result<Long> confirmOrder(ConfirmOrderDTO confirmOrderDTO);

    Result<Long> renewRent(RenewRentDTO renewRentDTO);

    Result<Long> returnBattery(ReturnBatteryDTO returnBatteryDTO);

    Result<Long> applyRefund(ApplyRefundDTO applyRefundDTO);

    Result<Long> returnBatteryConfirm(ConfirmOrderDTO confirmOrderDTO);

    Result<Long> agreeRefund(ConfirmOrderDTO confirmOrderDTO);

    Result<Long> disagreeRefund(DisagreeRefundDTO disagreeRefundDTO);

    Result<OutTimeOrderInfoVO> getTimeoutOrderInfo(Long orderId);

    Result<Long> payTimeoutOrder(PayTimeoutOrderDTO payTimeoutOrderDTO);

    Result<Long> informHandelTimeoutOrder(Long orderId);
}
