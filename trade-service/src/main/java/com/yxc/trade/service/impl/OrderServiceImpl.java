package com.yxc.trade.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.api.client.ItemClient;
import com.yxc.api.client.UserClient;
import com.yxc.api.po.*;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.RedisIdWork;
import com.yxc.common.utils.UserContext;
import com.yxc.trade.controller.AdminNotificationController;
import com.yxc.trade.controller.UserNotificationController;
import com.yxc.trade.domain.dto.*;
import com.yxc.trade.domain.enums.OrderStatus;
import com.yxc.trade.domain.po.Order;
import com.yxc.trade.domain.po.OrderDetail;
import com.yxc.trade.domain.vo.*;
import com.yxc.trade.mapper.OrderMapper;
import com.yxc.trade.mq.OrderProducerService;
import com.yxc.trade.service.OrderDetailService;
import com.yxc.trade.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    private RedissonClient redissonClient;

    @Resource
    private OrderDetailService orderDetailService;

    @Resource
    private AdminNotificationController adminNotificationController;

    @Resource
    private UserNotificationController userNotificationController;

    @Autowired
    private OrderProducerService orderProducerService;

    private static final String ORDER_LOCK_PREFIX = "lock:order:";

    @Override
    @GlobalTransactional
    public Result<CreateOrderVO> createOrder(CreateOrderDTO createOrderDTO) {
        Long itemId = createOrderDTO.getItemId();
        Integer days = createOrderDTO.getDays();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if (CollUtil.isEmpty(items)) {
            return Result.error("商品不存在");
        }
        if (days <= 0) {
            return Result.error("请输入恰当的租赁天数");
        }
        Item item = items.get(0);
        Order order = new Order();
        order.setOrderId(redisIdWork.nextId("order"));
        order.setItemId(item.getItemId());
        order.setUserId(UserContext.getUser().getUserId());
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
        if (item.getCapacity() == 0) {
            return Result.error("商品库存不足");
        }
        if (item.getStatus() == 1) {
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

        // 消息队列发送延时消息，处理超时订单
        orderProducerService.sendOrderCancelMessage(order);

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
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }
        BigDecimal totalFee = payOrderDTO.getTotalFee();

        List<User> users = userClient.getUserByIds(List.of(UserContext.getUser().getUserId()));
        if (CollUtil.isEmpty(users)) {
            return Result.error("用户不存在");
        }
        User user = users.get(0);
        BigDecimal balance = user.getBalance();
        if (balance.compareTo(totalFee) < 0) {
            return Result.error("余额不足");
        }

        DeductBalance deductBalance = new DeductBalance();
        deductBalance.setUserId(user.getUserId());
        deductBalance.setAmount(payOrderDTO.getTotalFee());
        Result<?> result = userClient.deductBalance(deductBalance);
        if (result.getCode() > 200) {
            return result;
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                if (order.getStatus() != OrderStatus.CREATED) {
                    return Result.error("订单状态异常，无法支付");
                }
                if (order.getTotalFee().compareTo(payOrderDTO.getTotalFee()) != 0) {
                    return Result.error("付款金额错误，请重新支付");
                }
                order.setStatus(OrderStatus.PAID);
                order.setPayTime(new Date());
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        if (order == null) {
            return Result.error("获取订单信息失败");
        }
        OrderDetail detail = new OrderDetail();
        detail.setCreatedAt(new Date());
        detail.setStatus(order.getStatus());
        detail.setOrderId(order.getOrderId());
        detail.setDescription("支付成功");
        orderDetailService.save(detail);

        adminNotificationController.sendNewOrderNotification(order);

        return Result.ok(orderId);
    }

    @Override
    public Result<PageVO<Order>> pageQuery(OrderPageQueryDTO pageQuery) {
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        String orderNo = pageQuery.getOrderNo();
        List<OrderStatus> status = pageQuery.getStatus();
        LocalDate startDate = pageQuery.getStartDate();
        LocalDate endDate = pageQuery.getEndDate();
        Page<Order> page = lambdaQuery()
                .in(status != null && !status.isEmpty(), Order::getStatus, status)
                .between(startDate != null && endDate != null, Order::getCreatedAt, startDate, endDate)
                .likeRight(StrUtil.isNotEmpty(orderNo), Order::getOrderId, orderNo)
                .orderBy(true, true, Order::getStatus)
                .orderBy(true, false, Order::getCreatedAt)
                .page(new Page<>(pageNo, pageSize));
        PageVO<Order> vo = new PageVO<>();
        vo.setPages(page.getPages());
        vo.setTotal(page.getTotal());
        vo.setList(page.getRecords());
        return Result.ok(vo);
    }

    @Override
    public Result<PageVO<Order>> pageQueryByUserId(OrderPageQueryDTO pageQuery) {
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        String orderNo = pageQuery.getOrderNo();
        List<OrderStatus> status = pageQuery.getStatus();
        LocalDate startDate = pageQuery.getStartDate();
        LocalDate endDate = pageQuery.getEndDate();

        Long userId = UserContext.getUser().getUserId();
        Page<Order> page = lambdaQuery()
                .eq(Order::getUserId, userId)
                .in(status != null && !status.isEmpty(), Order::getStatus, status)
                .between(startDate != null && endDate != null, Order::getCreatedAt, startDate, endDate)
                .likeRight(StrUtil.isNotEmpty(orderNo), Order::getOrderId, orderNo)
                .orderBy(true, true, Order::getStatus)
                .orderBy(true, false, Order::getCreatedAt)
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
        List<OrderDetail> orderDetails = orderDetailService
                .lambdaQuery()
                .eq(OrderDetail::getOrderId, orderId)
                .list();
        vo.setStatusLogs(orderDetails);
        return Result.ok(vo);
    }

    @Override
    @Transactional
    public Result<?> shipOrder(ShipOrderDTO shipOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("user")) {
            return Result.error("用户没有权限配送订单");
        }
        Long orderId = shipOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }
        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                if (order.getStatus() != OrderStatus.PAID) {
                    return Result.error("订单状态异常，无法配送");
                }
                order.setStatus(OrderStatus.SHIPPED);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        OrderDetail detail = new OrderDetail();
        detail.setStatus(OrderStatus.SHIPPED);
        detail.setOrderId(orderId);
        detail.setDescription("订单已配送");
        orderDetailService.save(detail);

        assert order != null;
        userNotificationController.sendShipOrderNotification(order.getUserId(), order);

        return Result.ok(1);
    }

    @Override
    @GlobalTransactional
    public Result<?> cancelOrder(CancelOrderDTO cancelOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("管理员无法取消订单"));
        }
        Long orderId = cancelOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        String cancelReason = cancelOrderDTO.getCancelReason();
        if (StrUtil.isEmpty(cancelReason) || cancelReason.length() < 4) {
            return Result.error("取消原因不能少于4字符");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAID) {
                    return Result.error("订单状态不为 待支付 或 待配送 状态，无法取消");
                }
                if (!Objects.equals(order.getUserId(), user.getUserId())) {
                    return Result.error("取消订单用户和创建用户不一致，无法取消订单");
                }
                order.setStatus(OrderStatus.CANCELLED);
                order.setActualEnd(new Date());
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        if (order == null) {
            return Result.error("获取订单信息失败");
        }

        // 订单已支付，退还支付金额
        if (status == OrderStatus.PAID) {
            ReturnBalance returnBalance = new ReturnBalance();
            returnBalance.setUserId(user.getUserId());
            returnBalance.setAmount(order.getTotalFee());
            userClient.returnBalance(returnBalance);
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.CANCELLED);
        detail.setDescription(
                StrUtil.isEmpty(cancelReason) ?
                        "订单已取消：用户未给出原因" :
                        "订单已取消，取消原因：" + cancelReason);
        orderDetailService.save(detail);

        // 商品库存归还
        DeductStock deductStock = new DeductStock();
        deductStock.setItemId(order.getItemId());
        deductStock.setNum(-1);
        itemClient.deductStock(List.of(deductStock));

        adminNotificationController.sendCancelOrderNotification(order);

        return Result.ok(1);
    }

    @Override
    @Transactional
    public Result<Long> confirmOrder(ConfirmOrderDTO confirmOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("管理员无法确认收货"));
        }
        Long orderId = confirmOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }
        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (order.getStatus() != OrderStatus.SHIPPED) {
                    return Result.error("订单状态不为 配送中 状态，无法确认收货");
                }
                if (!Objects.equals(order.getUserId(), user.getUserId())) {
                    return Result.error("确认收货用户和创建订单用户不一致，无法确认收货");
                }
                order.setStatus(OrderStatus.RENTING);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        OrderDetail detail = new OrderDetail();
        assert order != null;
        detail.setOrderId(order.getOrderId());
        detail.setStatus(OrderStatus.RENTING);
        detail.setDescription("用户确认收货");
        orderDetailService.save(detail);

        return Result.ok(1L);
    }

    @Override
    @GlobalTransactional
    public Result<Long> renewRent(RenewRentDTO renewRentDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("管理员无法进行续租"));
        }

        Long orderId = renewRentDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        Integer days = renewRentDTO.getDays();
        BigDecimal payAmount = renewRentDTO.getPayAmount();
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (order.getStatus() != OrderStatus.RENTING) {
                    return Result.error("订单状态不为 租赁中 状态，无法进行续租");
                }
                if (!Objects.equals(order.getUserId(), user.getUserId())) {
                    return Result.error("续租用户和创建订单用户不一致，无法进行续租");
                }
                order.setTotalFee(order.getTotalFee().add(payAmount));
                order.setExpectedEnd(order.getExpectedEnd().plusDays(days));
                order.setDays(order.getDays() + days);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        assert order != null;
        Long itemId = order.getItemId();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if (CollUtil.isEmpty(items)) {
            return Result.error("商品信息不存在");
        }
        BigDecimal shouldPayAmount = items.get(0).getPrice().multiply(new BigDecimal(days));
        if (payAmount.compareTo(shouldPayAmount) != 0) {
            return Result.error("支付金额错误，续租失败");
        }

        DeductBalance deductBalance = new DeductBalance();
        deductBalance.setUserId(user.getUserId());
        deductBalance.setAmount(shouldPayAmount);
        userClient.deductBalance(deductBalance);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(order.getOrderId());
        detail.setStatus(OrderStatus.RENTING);
        detail.setDescription("用户续租订单，商品租赁时间从 " + order.getExpectedEnd().plusDays(-days) + " 延长至 " + order.getExpectedEnd());
        orderDetailService.save(detail);

        return Result.ok(1L);
    }

    @Override
    @Transactional
    public Result<Long> returnBattery(ReturnBatteryDTO returnBatteryDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("管理员无法归还商品"));
        }
        Long orderId = returnBatteryDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (order.getStatus() != OrderStatus.RENTING) {
                    return Result.error("订单状态不为 租赁中 状态，无法归还电池");
                }
                if (!Objects.equals(order.getUserId(), user.getUserId())) {
                    return Result.error("电池归还用户和创建订单用户不一致，无法归还电池");
                }
                order.setStatus(OrderStatus.RETURNING);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.RETURNING);
        detail.setDescription("用户申请归还电池");
        orderDetailService.save(detail);

        adminNotificationController.sendReturnBatteryNotification(order);

        return Result.ok(1L);
    }

    @Override
    @Transactional
    public Result<Long> applyRefund(ApplyRefundDTO applyRefundDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("管理员无法申请退款"));
        }

        String refundReason = applyRefundDTO.getRefundReason();
        if (StrUtil.isEmpty(refundReason) || refundReason.length() < 10) {
            return Result.error("退款原因不能少于10字符");
        }

        Long orderId = applyRefundDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (!(status == OrderStatus.SHIPPED || status == OrderStatus.RENTING)) {
                    return Result.error("订单状态不为 已发货 或 租赁中 状态，无法申请退款");
                }
                if (!Objects.equals(order.getUserId(), user.getUserId())) {
                    return Result.error("申请退款用户和创建订单用户不一致，无法申请退款");
                }
                order.setStatus(OrderStatus.REFUNDING);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.REFUNDING);
        detail.setDescription("用户申请退款，退款原因：" + refundReason);
        orderDetailService.save(detail);

        adminNotificationController.sendRefundOrderNotification(order);

        return Result.ok(1L);
    }

    @Override
    @GlobalTransactional
    public Result<Long> returnBatteryConfirm(ConfirmOrderDTO confirmOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("user")) {
            return Result.error(("用户无法进行电池归还确认"));
        }

        Long orderId = confirmOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (status != OrderStatus.RETURNING) {
                    return Result.error("订单状态不为 归还中 状态，无法确认电池归还");
                }
                order.setStatus(OrderStatus.RETURNED);
                order.setActualEnd(new Date());
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 商品库存归还
        DeductStock deductStock = new DeductStock();
        assert order != null;
        deductStock.setItemId(order.getItemId());
        deductStock.setNum(-1);
        itemClient.deductStock(List.of(deductStock));

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.RETURNING);
        detail.setDescription("商家确认归还电池");
        orderDetailService.save(detail);

        userNotificationController.sendConfirmBatteryReturnNotification(order.getUserId(), order);

        return Result.ok(1L);
    }

    @Override
    @GlobalTransactional
    public Result<Long> agreeRefund(ConfirmOrderDTO confirmOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("user")) {
            return Result.error(("用户无法进行退款确认"));
        }

        Long orderId = confirmOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Integer days = 0;
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (status != OrderStatus.REFUNDING) {
                    return Result.error("订单状态不为 退款中 状态，无法同意退款");
                }
                days = order.getExpectedEnd().compareTo(LocalDate.now());
                order.setStatus(OrderStatus.REFUNDED);
                order.setActualEnd(new Date());
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        assert order != null;
        Long itemId = order.getItemId();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if (CollUtil.isEmpty(items)) {
            return Result.error("商品信息不存在");
        }
        BigDecimal amount = items.get(0).getPrice().multiply(new BigDecimal(days));

        // 用户退款
        ReturnBalance returnBalance = new ReturnBalance();
        returnBalance.setUserId(order.getUserId());
        returnBalance.setAmount(amount);
        log.info("returnBalance:{}", returnBalance);
        userClient.returnBalance(returnBalance);

        // 商品库存归还
        DeductStock deductStock = new DeductStock();
        deductStock.setItemId(order.getItemId());
        deductStock.setNum(-1);
        itemClient.deductStock(List.of(deductStock));


        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.REFUNDED);
        detail.setDescription("商家同意用户退款，退款金额：" + amount + "元");
        orderDetailService.save(detail);

        userNotificationController.sendAgreeRefundNotification(order.getUserId(), order);

        return Result.ok(1L);
    }

    @Override
    @Transactional
    public Result<Long> disagreeRefund(DisagreeRefundDTO disagreeRefundDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("user")) {
            return Result.error(("用户无法处理退款请求"));
        }

        Long orderId = disagreeRefundDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        String disagreeReason = disagreeRefundDTO.getDisagreeReason();
        if (StrUtil.isEmpty(disagreeReason) || disagreeReason.length() < 10) {
            return Result.error("拒绝原因不能少于10字符");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (status != OrderStatus.REFUNDING) {
                    return Result.error("订单状态不为 退款中 状态，无法退款");
                }
                order.setStatus(OrderStatus.RENTING);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.RENTING);
        detail.setDescription("商家拒绝用户退款，拒绝原因：" + disagreeReason);
        orderDetailService.save(detail);

        assert order != null;
        userNotificationController.sendDisagreeRefundNotification(order.getUserId(), order);

        return Result.ok(1L);
    }

    @Override
    public Result<OutTimeOrderInfoVO> getTimeoutOrderInfo(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            return Result.error("订单信息不存在");
        }
        LocalDate expectedEnd = order.getExpectedEnd();
        LocalDate now = LocalDate.now();
        int days = now.compareTo(expectedEnd);
        if (days <= 0) {
            return Result.error("订单未超时");
        }

        Long itemId = order.getItemId();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if (CollUtil.isEmpty(items)) {
            return Result.error("商品信息不存在");
        }
        BigDecimal price = items.get(0).getPrice();

        OutTimeOrderInfoVO vo = new OutTimeOrderInfoVO();
        vo.setOutTimeDays(days);
        vo.setOutTimeFee(price.multiply(new BigDecimal(days)));

        return Result.ok(vo);
    }

    @Override
    @GlobalTransactional
    public Result<Long> payTimeoutOrder(PayTimeoutOrderDTO payTimeoutOrderDTO) {
        UserInfo user = UserContext.getUser();
        if (user.getRole().equals("admin")) {
            return Result.error(("商家无法支付超时订单"));
        }

        Long orderId = payTimeoutOrderDTO.getOrderId();
        if (orderId == null) {
            return Result.error("订单号不能为空");
        }

        RLock lock = redissonClient.getLock(ORDER_LOCK_PREFIX + orderId);
        Order order = null;
        OrderStatus status = null;
        int days = payTimeoutOrderDTO.getDays();
        BigDecimal amount = payTimeoutOrderDTO.getPayAmount();
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                order = getById(orderId);
                status = order.getStatus();
                if (status != OrderStatus.TIMEOUT) {
                    return Result.error("订单状态不为 已超时 状态，无需处理");
                }

                LocalDate expectedEnd = order.getExpectedEnd();
                int timeoutDays = LocalDate.now().compareTo(expectedEnd);
                if (timeoutDays > days) {
                    return Result.error("支付金额不足，请重新支付");
                }
                order.setTotalFee(order.getTotalFee().add(amount));
                order.setExpectedEnd(expectedEnd.plusDays(days));
                order.setStatus(OrderStatus.RENTING);
                order.setDays(order.getDays() + days);
                updateById(order);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        assert order != null;
        Long itemId = order.getItemId();
        List<Item> items = itemClient.getItemByIds(List.of(itemId));
        if (CollUtil.isEmpty(items)) {
            return Result.error("商品信息不存在");
        }
        BigDecimal payAmount = items.get(0).getPrice().multiply(new BigDecimal(days));
        if (payAmount.compareTo(amount) != 0) {
            return Result.error("支付金额错误，请重新支付");
        }

        DeductBalance deductBalance = new DeductBalance();
        deductBalance.setUserId(user.getUserId());
        deductBalance.setAmount(payAmount);
        userClient.deductBalance(deductBalance);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orderId);
        detail.setStatus(OrderStatus.RENTING);
        detail.setDescription("用户支付超时费用：" + payAmount + "元，订单恢复正常");
        orderDetailService.save(detail);
        return Result.ok(1L);
    }

    @Override
    public Result<Long> informHandelTimeoutOrder(Long orderId) {
        Order order = getById(orderId);
        userNotificationController.sendTimeoutOrderNotification(order.getUserId(), order);
        return Result.ok(1L);
    }

    @Override
    public Result<GetCountByStatusVO> getCountByStatus() {
        List<Order> orders = lambdaQuery().list();
        Map<OrderStatus, Long> statusCount = orders.stream().collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        GetCountByStatusVO vo = new GetCountByStatusVO();
        vo.setPaid(statusCount.get(OrderStatus.PAID) == null ? 0 : statusCount.get(OrderStatus.PAID));
        vo.setRenting((statusCount.get(OrderStatus.RENTING) == null ? 0 : statusCount.get(OrderStatus.RENTING))
                + (statusCount.get(OrderStatus.SHIPPED) == null ? 0 : statusCount.get(OrderStatus.SHIPPED)));
        vo.setCanceled(statusCount.get(OrderStatus.CANCELLED) == null ? 0 : statusCount.get(OrderStatus.CANCELLED));
        vo.setRefunded(statusCount.get(OrderStatus.REFUNDED) == null ? 0 : statusCount.get(OrderStatus.REFUNDED));
        vo.setTimeout(statusCount.get(OrderStatus.TIMEOUT) == null ? 0 : statusCount.get(OrderStatus.TIMEOUT));
        vo.setTotal((long) orders.size());

        return Result.ok(vo);
    }

    @Override
    public Result<TodayOrderDataVO> getTodayOrderData() {
        LocalDate today = LocalDate.now();
        List<Order> orders = lambdaQuery().eq(Order::getStartTime, today).list();
        if(orders.isEmpty()) {
            TodayOrderDataVO vo = new TodayOrderDataVO();
            vo.setTurnover(new BigDecimal("0.00"));
            vo.setEffectiveOrder(0L);
            vo.setOrderCompleteRate(new BigDecimal("0.00"));
            vo.setAvgOrderPrice(new BigDecimal("0.00"));
            return Result.ok(vo);
        }
        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);

        long effectiveOrder = 0L;
        BigDecimal turnover = new BigDecimal("0.00");
        for(Order order : orders) {
            if(effectiveStatus.contains(order.getStatus())) {
                effectiveOrder++;
                turnover = turnover.add(order.getTotalFee());
            }
        }

        BigDecimal orderCompleteRate, avgOrderPrice;
        if(effectiveOrder == 0) {
            orderCompleteRate = new BigDecimal("0.00");
            avgOrderPrice = new BigDecimal("0.00");
        } else {
            orderCompleteRate = new BigDecimal(effectiveOrder)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(orders.size()), 2, RoundingMode.HALF_DOWN);
            avgOrderPrice = turnover.divide(new BigDecimal(effectiveOrder), 2, RoundingMode.HALF_DOWN);
        }

        TodayOrderDataVO vo = new TodayOrderDataVO();
        vo.setTurnover(turnover);
        vo.setEffectiveOrder(effectiveOrder);
        vo.setOrderCompleteRate(orderCompleteRate);
        vo.setAvgOrderPrice(avgOrderPrice);

        return Result.ok(vo);
    }

    @Override
    public Result<GetTurnoverVO> getTodayData() {
        LocalDate today = LocalDate.now();
        List<Order> orders = lambdaQuery().eq(Order::getStartTime, today).list();
        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);
        Map<String, Integer> itemSaleMap = new HashMap<>();
        long effectiveOrder = 0L;
        BigDecimal turnover = new BigDecimal("0.00");
        for(Order order : orders) {
            if(effectiveStatus.contains(order.getStatus())) {
                effectiveOrder++;
                turnover = turnover.add(order.getTotalFee());
                String itemName = order.getItemName();
                if(itemSaleMap.containsKey(itemName)) {
                    itemSaleMap.put(itemName, itemSaleMap.get(itemName) + order.getDays());
                } else {
                    itemSaleMap.put(itemName, order.getDays());
                }
            }
        }

        List<ItemSaleVO> itemSaleVOList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            ItemSaleVO vo = new ItemSaleVO();
            vo.setName(entry.getKey());
            vo.setSales(entry.getValue());
            itemSaleVOList.add(vo);
        }
        itemSaleVOList.sort((o1, o2) -> o2.getSales() - o1.getSales());

        GetTurnoverVO vo = new GetTurnoverVO();
        vo.setTurnovers(List.of(turnover));
        vo.setTotalOrders(Collections.singletonList((long) orders.size()));
        vo.setEffectiveOrders(Collections.singletonList(effectiveOrder));
        vo.setItemSales(itemSaleVOList.subList(0, Math.min(itemSaleVOList.size(), 10)));
        return Result.ok(vo);
    }

    @Override
    public Result<GetTurnoverVO> getYesterday() {
        LocalDate yesterday = LocalDate.now().plusDays(-1L);
        List<Order> orders = lambdaQuery().eq(Order::getStartTime, yesterday).list();
        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);

        Map<String, Integer> itemSaleMap = new HashMap<>();
        long effectiveOrder = 0L;
        BigDecimal turnover = new BigDecimal("0.00");
        for(Order order : orders) {
            if(effectiveStatus.contains(order.getStatus())) {
                effectiveOrder++;
                turnover = turnover.add(order.getTotalFee());
                String itemName = order.getItemName();
                if(itemSaleMap.containsKey(itemName)) {
                    itemSaleMap.put(itemName, itemSaleMap.get(itemName) + order.getDays());
                } else {
                    itemSaleMap.put(itemName, order.getDays());
                }
            }
        }

        List<ItemSaleVO> itemSaleVOList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            ItemSaleVO vo = new ItemSaleVO();
            vo.setName(entry.getKey());
            vo.setSales(entry.getValue());
            itemSaleVOList.add(vo);
        }
        itemSaleVOList.sort((o1, o2) -> o2.getSales() - o1.getSales());

        GetTurnoverVO vo = new GetTurnoverVO();
        vo.setTurnovers(List.of(turnover));
        vo.setTotalOrders(Collections.singletonList((long) orders.size()));
        vo.setEffectiveOrders(Collections.singletonList(effectiveOrder));
        vo.setItemSales(itemSaleVOList.subList(0, Math.min(itemSaleVOList.size(), 10)));

        return Result.ok(vo);
    }

    @Override
    public Result<GetTurnoverVO> getLast7days() {
        LocalDate[] days = new LocalDate[7];
        LocalDate today = LocalDate.now();
        for(int i = 6; i >= 0; i--) {
            days[i] = today.plusDays(i - 6);
        }

        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);
        List<BigDecimal> turnovers = new ArrayList<>();
        List<Long> totalOrders = new ArrayList<>();
        List<Long> effectiveOrders = new ArrayList<>();
        Map<String, Integer> itemSaleMap = new HashMap<>();
        for(LocalDate date : days) {
            List<Order> orders = lambdaQuery().eq(Order::getStartTime, date).list();
            long effectiveOrder = 0L;
            BigDecimal turnover = new BigDecimal("0.00");
            for(Order order : orders) {
                if(effectiveStatus.contains(order.getStatus())) {
                    effectiveOrder++;
                    turnover = turnover.add(order.getTotalFee());
                    String itemName = order.getItemName();
                    if(itemSaleMap.containsKey(itemName)) {
                        itemSaleMap.put(itemName, itemSaleMap.get(itemName) + order.getDays());
                    } else {
                        itemSaleMap.put(itemName, order.getDays());
                    }
                }
            }
            turnovers.add(turnover);
            totalOrders.add((long) orders.size());
            effectiveOrders.add(effectiveOrder);
        }

        List<ItemSaleVO> itemSaleVOList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            ItemSaleVO vo = new ItemSaleVO();
            vo.setName(entry.getKey());
            vo.setSales(entry.getValue());
            itemSaleVOList.add(vo);
        }
        itemSaleVOList.sort((o1, o2) -> o2.getSales() - o1.getSales());

        GetTurnoverVO vo = new GetTurnoverVO();
        vo.setTotalOrders(totalOrders);
        vo.setEffectiveOrders(effectiveOrders);
        vo.setTurnovers(turnovers);
        vo.setItemSales(itemSaleVOList.subList(0, Math.min(itemSaleVOList.size(), 10)));

        return Result.ok(vo);
    }

    @Override
    public Result<GetTurnoverVO> getLast15days() {
        LocalDate[] days = new LocalDate[15];
        LocalDate today = LocalDate.now();
        for(int i = 14; i >= 0; i--) {
            days[i] = today.plusDays(i - 14);
        }

        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);
        List<BigDecimal> turnovers = new ArrayList<>();
        List<Long> totalOrders = new ArrayList<>();
        List<Long> effectiveOrders = new ArrayList<>();
        Map<String, Integer> itemSaleMap = new HashMap<>();
        for(LocalDate date : days) {
            List<Order> orders = lambdaQuery().eq(Order::getStartTime, date).list();
            long effectiveOrder = 0L;
            BigDecimal turnover = new BigDecimal("0.00");
            for(Order order : orders) {
                if(effectiveStatus.contains(order.getStatus())) {
                    effectiveOrder++;
                    turnover = turnover.add(order.getTotalFee());
                    String itemName = order.getItemName();
                    if(itemSaleMap.containsKey(itemName)) {
                        itemSaleMap.put(itemName, itemSaleMap.get(itemName) + order.getDays());
                    } else {
                        itemSaleMap.put(itemName, order.getDays());
                    }
                }
            }
            turnovers.add(turnover);
            totalOrders.add((long) orders.size());
            effectiveOrders.add(effectiveOrder);
        }

        List<ItemSaleVO> itemSaleVOList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            ItemSaleVO vo = new ItemSaleVO();
            vo.setName(entry.getKey());
            vo.setSales(entry.getValue());
            itemSaleVOList.add(vo);
        }
        itemSaleVOList.sort((o1, o2) -> o2.getSales() - o1.getSales());

        GetTurnoverVO vo = new GetTurnoverVO();
        vo.setTotalOrders(totalOrders);
        vo.setEffectiveOrders(effectiveOrders);
        vo.setTurnovers(turnovers);
        vo.setItemSales(itemSaleVOList.subList(0, Math.min(itemSaleVOList.size(), 10)));

        return Result.ok(vo);
    }

    @Override
    public Result<GetTurnoverVO> getLast30days() {
        LocalDate[] days = new LocalDate[30];
        LocalDate today = LocalDate.now();
        for(int i = 29; i >= 0; i--) {
            days[i] = today.plusDays(i - 29);
        }

        Set<OrderStatus> effectiveStatus = Set.of(
                OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.RENTING,
                OrderStatus.REFUNDING, OrderStatus.RETURNING, OrderStatus.RETURNED);
        List<BigDecimal> turnovers = new ArrayList<>();
        List<Long> totalOrders = new ArrayList<>();
        List<Long> effectiveOrders = new ArrayList<>();
        Map<String, Integer> itemSaleMap = new HashMap<>();
        for(LocalDate date : days) {
            List<Order> orders = lambdaQuery().eq(Order::getStartTime, date).list();
            long effectiveOrder = 0L;
            BigDecimal turnover = new BigDecimal("0.00");
            for(Order order : orders) {
                if(effectiveStatus.contains(order.getStatus())) {
                    effectiveOrder++;
                    turnover = turnover.add(order.getTotalFee());
                    String itemName = order.getItemName();
                    if(itemSaleMap.containsKey(itemName)) {
                        itemSaleMap.put(itemName, itemSaleMap.get(itemName) + order.getDays());
                    } else {
                        itemSaleMap.put(itemName, order.getDays());
                    }
                }
            }
            turnovers.add(turnover);
            totalOrders.add((long) orders.size());
            effectiveOrders.add(effectiveOrder);
        }

        List<ItemSaleVO> itemSaleVOList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : itemSaleMap.entrySet()) {
            ItemSaleVO vo = new ItemSaleVO();
            vo.setName(entry.getKey());
            vo.setSales(entry.getValue());
            itemSaleVOList.add(vo);
        }
        itemSaleVOList.sort((o1, o2) -> o2.getSales() - o1.getSales());

        GetTurnoverVO vo = new GetTurnoverVO();
        vo.setTotalOrders(totalOrders);
        vo.setEffectiveOrders(effectiveOrders);
        vo.setTurnovers(turnovers);
        vo.setItemSales(itemSaleVOList.subList(0, Math.min(itemSaleVOList.size(), 10)));

        return Result.ok(vo);
    }

    @Override
    public List<Order> getOrderByItemId(Long id) {
        return lambdaQuery()
                .eq(Order::getItemId, id)
                .list();
    }

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(10);
        System.out.println(plusDays.compareTo(now));
    }

}
