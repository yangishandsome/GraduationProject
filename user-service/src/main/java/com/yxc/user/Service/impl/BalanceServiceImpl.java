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
import com.yxc.user.domain.po.BalanceRecords;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.AddBalanceVO;
import com.yxc.user.mapper.BalanceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class BalanceServiceImpl extends ServiceImpl<BalanceMapper, BalanceRecords> implements BalanceService {
    @Resource
    private UserService userService;

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
        Long userId = UserContext.getUser();
        synchronized (userId.toString().intern()) {
            User user = userService.getById(userId);
            BigDecimal newBalance = user.getBalance().add(amount);
            user.setBalance(newBalance);
            vo.setBalance(newBalance);
            userService.updateById(user);
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
                .eq(BalanceRecords::getUserId, UserContext.getUser())
                .orderBy(true, false, BalanceRecords::getCreateAt)
                .page(new Page<>(pageNo, pageSize));
        PageVO<BalanceRecords> vo = new PageVO<>();
        vo.setList(page.getRecords());
        vo.setTotal(page.getTotal());
        vo.setPages(page.getPages());
        return Result.ok(vo);
    }

    @Override
    public Result<?> deductBalance(DeductBalanceDTO deductBalanceDTO) {
        User user = userService.getById(deductBalanceDTO.getUserId());
        if(user.getBalance().compareTo(deductBalanceDTO.getAmount()) < 0) {
            return Result.error("余额不足");
        }
        user.setBalance(user.getBalance().subtract(deductBalanceDTO.getAmount()));
        userService.updateById(user);
        BalanceRecords records = new BalanceRecords();
        records.setPayMethod("payment");
        records.setPayStatus("success");
        records.setUserId(user.getUserId());
        records.setAmount(deductBalanceDTO.getAmount().negate());
        save(records);
        return Result.ok(1);
    }
}
