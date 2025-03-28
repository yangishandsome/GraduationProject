package com.yxc.user;

import com.yxc.common.utils.PasswordEncoder;
import com.yxc.user.Service.AdminService;
import com.yxc.user.Service.UserService;
import com.yxc.user.domain.po.Admin;
import com.yxc.user.domain.po.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
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

    @Test
    public void test1() {
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordEncoder.encode("123456"));
        admin.setStatus((short) 0);
        adminService.save(admin);
    }
}
