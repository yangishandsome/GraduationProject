package com.yxc.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.api.po.OrderDetail;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.mapper.OrderMapper;
import com.yxc.trade.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private ItemClient itemClient;

    @Override
    public List<Item> getItemByIds(List<Long> ids) {
        return itemClient.getItemByIds(ids);
    }

    @Override
    @GlobalTransactional
    public void test() {
        List<OrderDetail> list = new ArrayList<>();
        list.add(new OrderDetail(15L, 100));
        itemClient.deductStock(list);
        Order order = new Order();
        order.setUserId(28L);
        order.setItemId(1L);
        save(order);
        order.setItemId(null);
        save(order);
    }
}
