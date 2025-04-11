package com.yxc.user;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yxc.common.utils.PasswordEncoder;
import com.yxc.user.Service.AdminService;
import com.yxc.user.Service.BalanceService;
import com.yxc.user.Service.UserService;
import com.yxc.user.domain.po.Admin;
import com.yxc.user.domain.po.BalanceRecords;
import com.yxc.user.domain.po.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserService userService;
    @Resource
    private AdminService adminService;
    @Resource
    private BalanceService balanceService;

    @Test
    public void test1() {
        balanceService.remove(new LambdaQueryWrapper<BalanceRecords>()
                .le(BalanceRecords::getAmount, new BigDecimal("0.0")));
    }
}
