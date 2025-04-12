package com.yxc.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.api.po.Item;
import com.yxc.trade.domain.po.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    List<Item> getItemByIds(List<Long> ids);

    void test();
}
