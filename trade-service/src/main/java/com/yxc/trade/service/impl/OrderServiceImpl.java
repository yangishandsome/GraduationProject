package com.yxc.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.api.client.ItemClient;
import com.yxc.api.client.UserClient;
import com.yxc.api.po.DeductBalance;
import com.yxc.api.po.Item;
import com.yxc.api.po.DeductStock;
import com.yxc.api.po.User;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.common.utils.RedisIdWork;
import com.yxc.common.utils.UserContext;
import com.yxc.trade.domain.dto.CreateOrderDTO;
import com.yxc.trade.domain.dto.PayOrderDTO;
import com.yxc.trade.domain.enums.OrderStatus;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.domain.vo.CreateOrderVO;
import com.yxc.trade.domain.vo.OrderDetailVO;
import com.yxc.trade.mapper.OrderMapper;
import com.yxc.trade.service.OrderDetailService;
import com.yxc.trade.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private ItemClient itemClient;

    @Resource
    private UserClient userClient;

    @Resource
    private RedisIdWork redisIdWork;

    @Resource
    private OrderDetailService orderDetailService;

    @Override
    @GlobalTransactional
    public Result<CreateOrderVO> createOrder(CreateOrderDTO createOrderDTO) {
        Long itemId = createOrderDTO.getItemId();
        Integer days = createOrderDTO.getDays();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if(CollUtil.isEmpty(items)) {
            return Result.error("商品不存在");
        }
        if(days <= 0) {
            return Result.error("请输入恰当的租赁天数");
        }
        Item item = items.get(0);
        Order order = new Order();
        order.setOrderId(redisIdWork.nextId("order"));
        order.setItemId(item.getItemId());
        order.setUserId(UserContext.getUser());
        order.setItemName(item.getName());
        order.setItemImageUrl(item.getImageUrl());
        BigDecimal totalFee = item.getPrice().multiply(new BigDecimal(String.valueOf(days)));
        order.setTotalFee(totalFee);
        order.setStatus(OrderStatus.CREATED);
        order.setDays(days);
        order.setAddressDetail(createOrderDTO.getAddressDetail());
        order.setContactPhone(createOrderDTO.getContactPhone());
        order.setReceiverName(createOrderDTO.getReceiverName());

        order.setStartTime(LocalDate.now());
        order.setExpectedEnd(LocalDate.now().plusDays(days));
        if(item.getCapacity() == 0) {
            return Result.error("商品库存不足");
        }
        if(item.getStatus() == 1) {
            return Result.error("商品已下架");
        }
        DeductStock deductStock = new DeductStock();
        deductStock.setItemId(itemId);
        deductStock.setNum(1);
        try {
            itemClient.deductStock(List.of(deductStock));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
        save(order);

        OrderDetail detail = new OrderDetail();
        detail.setCreatedAt(new Date());
        detail.setStatus(order.getStatus());
        detail.setOrderId(order.getOrderId());
        detail.setDescription("订单已创建");
        orderDetailService.save(detail);

        CreateOrderVO vo = BeanUtil.copyProperties(order, CreateOrderVO.class);
        return Result.ok(vo);
    }

    @Override
    @GlobalTransactional
    public Result<?> payOrder(PayOrderDTO payOrderDTO) {
        Long orderId = payOrderDTO.getOrderId();
        log.info("orderId:{}", orderId);
        BigDecimal totalFee = payOrderDTO.getTotalFee();
        Order order = getById(orderId);
        if(order.getTotalFee().compareTo(payOrderDTO.getTotalFee()) != 0) {
            return Result.error("付款金额错误，请重新支付");
        }

        List<User> users = userClient.getUserByIds(List.of(UserContext.getUser()));
        if(CollUtil.isEmpty(users)) {
            return Result.error("用户不存在");
        }
        User user = users.get(0);
        BigDecimal balance = user.getBalance();
        if(balance.compareTo(totalFee) < 0) {
            return Result.error("余额不足");
        }

        DeductBalance deductBalance = new DeductBalance();
        deductBalance.setUserId(user.getUserId());
        deductBalance.setAmount(payOrderDTO.getTotalFee());
        Result<?> result = userClient.deductBalance(deductBalance);
        if(result.getCode() > 200) {
            return result;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPayTime(new Date());
        updateById(order);

        OrderDetail detail = new OrderDetail();
        detail.setCreatedAt(new Date());
        detail.setStatus(order.getStatus());
        detail.setOrderId(order.getOrderId());
        detail.setDescription("支付成功");
        orderDetailService.save(detail);

        return Result.ok(orderId);
    }

    @Override
    public Result<PageVO<Order>> pageQuery(PageQuery pageQuery) {
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        String likeName = pageQuery.getLikeName();
        Long userId = UserContext.getUser();
        Page<Order> page = lambdaQuery()
                .eq(Order::getUserId, userId)
                .orderBy(true, true, Order::getCreatedAt)
                .like(StrUtil.isNotEmpty(likeName), Order::getItemName, likeName)
                .page(new Page<>(pageNo, pageSize));
        PageVO<Order> vo = new PageVO<>();
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        return Result.ok(vo);
    }

    @Override
    public Result<OrderDetailVO> getOrderDetail(Long orderId) {
        Order order = getById(orderId);
        OrderDetailVO vo = BeanUtil.copyProperties(order, OrderDetailVO.class);
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();
        vo.setStatusLogs(orderDetails);
        return Result.ok(vo);
    }

    public static void main(String[] args) {
        BigDecimal b1 = new BigDecimal("1.0");
        BigDecimal b2 = new BigDecimal("1");
        System.out.println(b1.compareTo(b2));
    }

}
