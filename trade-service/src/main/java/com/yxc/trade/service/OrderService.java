package com.yxc.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.api.po.Item;
import com.yxc.trade.domain.po.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {

    List<Item> getItemByIds(List<Long> ids);
}
