package com.yxc.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.api.client.ItemClient;
import com.yxc.api.po.Item;
import com.yxc.trade.domain.po.Orders;
import com.yxc.trade.mapper.OrderMapper;
import com.yxc.trade.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Resource
    private ItemClient itemClient;

    @Override
    public List<Item> getItemByIds(List<Long> ids) {
        return itemClient.getItemByIds(ids);
    }
}
