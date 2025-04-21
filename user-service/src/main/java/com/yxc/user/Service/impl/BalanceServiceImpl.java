package com.yxc.user.Service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.BalanceService;
import com.yxc.user.Service.UserService;
import com.yxc.user.domain.dto.AddBalanceDTO;
import com.yxc.user.domain.dto.DeductBalanceDTO;
import com.yxc.user.domain.dto.ReturnBalanceDTO;
import com.yxc.user.domain.po.BalanceRecords;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.AddBalanceVO;
import com.yxc.user.mapper.BalanceMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class BalanceServiceImpl extends ServiceImpl<BalanceMapper, BalanceRecords> implements BalanceService {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:order:";

    @Override
    @Transactional
    public Result<AddBalanceVO> addBalance(AddBalanceDTO addBalanceDTO) {
        BigDecimal amount = addBalanceDTO.getAmount();
        String payMethod = addBalanceDTO.getPayMethod();
        if (amount.doubleValue() <= 0) {
            return Result.error("充值金额不能小于0");
        }
        if(StrUtil.isEmpty(payMethod)) {
            return Result.error("请选择正确的支付方式");
        }
        AddBalanceVO vo = new AddBalanceVO();
        Long userId = UserContext.getUser().getUserId();
        RLock lock = redissonClient.getLock(LOCK_PREFIX + userId);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                User user = userService.getById(userId);
                BigDecimal newBalance = user.getBalance().add(amount);
                user.setBalance(newBalance);
                vo.setBalance(newBalance);
                userService.updateById(user);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        BalanceRecords records = new BalanceRecords();
        records.setUserId(userId);
        records.setPayMethod(payMethod);
        records.setPayStatus("success");
        records.setAmount(amount);
        save(records);
        return Result.ok(vo);
    }

    @Override
    public Result<PageVO<BalanceRecords>> getBalanceRecords(PageQuery pageQuery) {
        Integer pageNo = pageQuery.getPageNo();
        Integer pageSize = pageQuery.getPageSize();
        Page<BalanceRecords> page = lambdaQuery()
                .eq(BalanceRecords::getUserId, UserContext.getUser().getUserId())
                .orderBy(true, false, BalanceRecords::getCreateAt)
                .page(new Page<>(pageNo, pageSize));
        PageVO<BalanceRecords> vo = new PageVO<>();
        vo.setList(page.getRecords());
        vo.setTotal(page.getTotal());
        vo.setPages(page.getPages());
        return Result.ok(vo);
    }

    @Override
    @Transactional
    public Result<?> deductBalance(DeductBalanceDTO deductBalanceDTO) {
        Long userId = deductBalanceDTO.getUserId();
        RLock lock = redissonClient.getLock(LOCK_PREFIX + userId);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                User user = userService.getById(userId);
                if(user.getBalance().compareTo(deductBalanceDTO.getAmount()) < 0) {
                    return Result.error("余额不足");
                }
                user.setBalance(user.getBalance().subtract(deductBalanceDTO.getAmount()));
                userService.updateById(user);
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        } finally {
            // 解锁前检查当前线程是否持有该锁
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        BalanceRecords records = new BalanceRecords();
        records.setPayMethod("payment");
        records.setPayStatus("success");
        records.setUserId(userId);
        records.setAmount(deductBalanceDTO.getAmount().negate());
        save(records);
        return Result.ok(1);
    }

    @Override
    @Transactional
    public Result<?> returnBalance(ReturnBalanceDTO returnBalanceDTO) {
        User user = userService.getById(returnBalanceDTO.getUserId());
        user.setBalance(user.getBalance().add(returnBalanceDTO.getAmount()));
        userService.updateById(user);

        BalanceRecords records = new BalanceRecords();
        records.setPayMethod("return");
        records.setPayStatus("success");
        records.setUserId(user.getUserId());
        records.setAmount(returnBalanceDTO.getAmount());
        save(records);
        return Result.ok(1);
    }
}
