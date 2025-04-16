package com.yxc.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.mapper.OrderDetailMapper;
import com.yxc.trade.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
