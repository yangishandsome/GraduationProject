package com.yxc.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.api.po.Item;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.trade.domain.dto.CreateOrderDTO;
import com.yxc.trade.domain.dto.PayOrderDTO;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.vo.CreateOrderVO;
import com.yxc.trade.domain.vo.OrderDetailVO;

import java.util.List;

public interface OrderService extends IService<Order> {

    Result<CreateOrderVO> createOrder(CreateOrderDTO createOrderDTO);

    Result<?> payOrder(PayOrderDTO payOrderDTO);

    Result<PageVO<Order>> pageQuery(PageQuery pageQuery);

    Result<OrderDetailVO> getOrderDetail(Long orderId);
}
