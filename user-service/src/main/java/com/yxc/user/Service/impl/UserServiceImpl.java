package com.yxc.user.Service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.Result;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.PasswordEncoder;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.AddressService;
import com.yxc.user.Service.UserService;
import com.yxc.user.config.JwtProperties;
import com.yxc.user.domain.dto.RegisterDTO;
import com.yxc.user.domain.dto.RegisterVerifyDTO;
import com.yxc.user.domain.dto.UpdateUserInfoDTO;
import com.yxc.user.domain.po.Address;
import com.yxc.user.domain.po.Register;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.*;
import com.yxc.user.mapper.UserMapper;
import com.yxc.user.utils.JwtTool;
import com.yxc.common.utils.RedisIdWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private AddressService addressService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JwtTool jwtTool;
    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private RedisIdWork redisIdWork;

    @Override
    public Result<LoginVO> login(String username, String password) {
        User user = lambdaQuery().eq(User::getUsername, username)
                .or().eq(User::getPhone, username)
                .one();
        if(user == null) {
            return Result.error("用户名不存在");
        }
        Boolean matches = PasswordEncoder.matches(user.getPasswordHash(), password);
        if(!matches) {
            return Result.error("密码错误");
        }
        LoginVO vo = new LoginVO();
        UserInfo userInfo = new UserInfo(user.getUserId(), "user", 0);
        String token = jwtTool.createToken(JSONUtil.toJsonStr(userInfo), jwtProperties.getTokenTTL());
        vo.setToken(token);
        vo.setUsername(user.getUsername());
        vo.setUserId(user.getUserId());
        log.info("用户：{}登录成功，用户id：{}", vo.getUsername(), vo.getUserId());
        lambdaUpdate().set(User::getLoginAt, LocalDateTime.now()).eq(User::getUserId, user.getUserId()).update();
        return Result.ok(vo);
    }

    @Override
    public Result<Long> sendCode(String phone) {
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set("user:code:" + phone, code, 5, TimeUnit.MINUTES);
        log.info("已为手机号：{}发送验证码：{}", phone, code);
        return Result.ok(1L);
    }

    @Override
    public Result<String> randomUsername() {
        String randomUsername = "用户" + Long.toHexString(redisIdWork.nextId("username"));
        User user;
        while ((user = lambdaQuery().eq(User::getUsername, randomUsername).one()) != null) {
            randomUsername = "用户" + Long.toHexString(redisIdWork.nextId("username"));
        }
        return Result.ok(randomUsername);
    }

    @Override
    public Result<RegisterVO> register(RegisterDTO registerDTO) {
        String username = registerDTO.getUsername();
        String phone = registerDTO.getPhone();
        String password = registerDTO.getPassword();
        if(StrUtil.isEmpty(username)) {
            return Result.error("用户名不能为空");
        }
        if(StrUtil.isEmpty(phone)) {
            return Result.error("手机号不能为空");
        }
        if(StrUtil.isEmpty(password)) {
            return Result.error("密码不能为空");
        }
        List<User> list = lambdaQuery().eq(User::getPhone, phone).or().eq(User::getUsername, username).list();
        Set<String> usernameSet = list.stream().map(User::getUsername).collect(Collectors.toSet());
        if(usernameSet.contains(username)) {
            return Result.error("用户名已存在");
        }
        Set<String> phoneSet = list.stream().map(User::getPhone).collect(Collectors.toSet());
        if(phoneSet.contains(phone)) {
            return Result.error("手机号已注册");
        }
        Register register = new Register();
        register.setUsername(username);
        register.setPhone(phone);
        register.setPassword(password);
        stringRedisTemplate.opsForValue().set("user:register:" + phone, JSONUtil.toJsonStr(register), 2, TimeUnit.HOURS);
        sendCode(phone);
        RegisterVO vo = new RegisterVO();
        vo.setPhone(phone);
        log.info("用户：{}注册中，", vo.getPhone());
        return Result.ok(vo);
    }

    @Override
    public Result<RegisterVerifyVO> registerVerify(RegisterVerifyDTO registerVerifyDTO) {
        String phone = registerVerifyDTO.getPhone();
        String code = stringRedisTemplate.opsForValue().getAndDelete("user:code:" + phone);
        if(StrUtil.isEmpty(code)) {
            return Result.error("验证码已过期");
        }
        if(!code.equals(registerVerifyDTO.getCode())) {
            return Result.error("验证码错误");
        }
        String jsonStr = stringRedisTemplate.opsForValue().getAndDelete("user:register:" + phone);
        if(StrUtil.isEmpty(jsonStr)) {
            return Result.error("用户不存在，请重新注册");
        }
        Register register = JSONUtil.toBean(jsonStr, Register.class);
        User user = new User();
        user.setPhone(phone);
        user.setUsername(register.getUsername());
        user.setPasswordHash(PasswordEncoder.encode(register.getPassword()));
        save(user);
        UserInfo userInfo = new UserInfo(user.getUserId(), "user", 0);
        String token = jwtTool.createToken(JSONUtil.toJsonStr(userInfo), jwtProperties.getTokenTTL());
        RegisterVerifyVO vo = new RegisterVerifyVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setToken(token);
        log.info("用户：{}注册成功，token：{}", vo.getUsername(), token);
        return Result.ok(vo);
    }

    @Override
    public Result<UserInfoVO> getUserInfo(Long userId) {
        User user = getById(userId);
        if(user == null) {
            return Result.error("用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        Address address = addressService.lambdaQuery()
                .eq(Address::getUserId, userId).eq(Address::getIsDefault, 1)
                .one();
        if(address != null) {
            address.setProvince(StrUtil.isEmpty(address.getProvince()) ? "" : address.getProvince());
            String addressDetail = address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail();
            vo.setAddress(addressDetail);
        }
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setBalance(user.getBalance());
        return Result.ok(vo);
    }

    @Override
    public Result<Long> updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO) {
        String username = updateUserInfoDTO.getUsername();
        String email = updateUserInfoDTO.getEmail();
        String phone = updateUserInfoDTO.getPhone();
        Long userId = UserContext.getUser().getUserId();
        User user = lambdaQuery()
                .eq(StrUtil.isNotEmpty(username), User::getUsername, username)
                .or().eq(StrUtil.isNotEmpty(email), User::getEmail, email)
                .or().eq(StrUtil.isNotEmpty(phone), User::getPhone, phone)
                .one();
        if(user != null && !user.getUserId().equals(userId)) {
            String msg = "";
            if(user.getUsername().equals(username)) {
                msg = "用户名已存在";
            } else if(user.getPhone().equals(phone)) {
                msg = "手机号已被使用";
            } else {
                msg = "邮箱已被使用";
            }
            return Result.error(msg);
        }
        if((user = getById(userId)) == null) {
            return Result.error("用户不存在");
        }
        if(StrUtil.isNotEmpty(username)) {
            user.setUsername(username);
        }
        if(StrUtil.isNotEmpty(phone)) {
            user.setPhone(phone);
        }
        if(StrUtil.isNotEmpty(email)) {
            user.setEmail(email);
        }
        updateById(user);
        return Result.ok(1L);
    }

    @Override
    public List<User> getUserByIds(List<Long> ids) {
        return userMapper.selectBatchIds(ids);
    }

    @Override
    public Result<TodayUserDataVO> getTodayUserData() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        Long newUser = lambdaQuery().between(User::getCreatedAt, begin, end).count();
        Long activeUser = lambdaQuery().between(User::getLoginAt, begin, end).count();

        TodayUserDataVO todayUserDataVO = new TodayUserDataVO();
        todayUserDataVO.setActiveUser(activeUser);
        todayUserDataVO.setNewUser(newUser);

        return Result.ok(todayUserDataVO);
    }

    @Override
    public Result<GetUserDataVO> getLast7days() {
        LocalDateTime begin = LocalDateTime.now().plusDays(-6).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LocalDate[] dates = new LocalDate[7];
        LocalDate today = LocalDate.now();
        for(int i = 6; i >= 0; i--) {
            dates[i] = today.plusDays(i - 6);
        }

        List<User> newUser = lambdaQuery().between(User::getCreatedAt, begin, end).list();
        Long totalUser = lambdaQuery().count();

        Map<LocalDate, Long> newUserCountMap = newUser.stream()
                .collect(Collectors.groupingBy(user -> user.getCreatedAt().toLocalDate(), Collectors.counting()));

        GetUserDataVO vo = new GetUserDataVO();
        List<Long> totalUserCount = new ArrayList<>();
        List<Long> newUserCount = new ArrayList<>();
        for(LocalDate date : dates) {
            Long todayNewUser = newUserCountMap.getOrDefault(date, 0L);
            totalUserCount.add(totalUser);
            newUserCount.add(todayNewUser);
            totalUser -= todayNewUser;
        }
        vo.setTotalCounts(CollUtil.reverse(totalUserCount));
        vo.setNewCounts(newUserCount);

        return Result.ok(vo);
    }

    @Override
    public Result<GetUserDataVO> getLast15days() {
        LocalDateTime begin = LocalDateTime.now().plusDays(-14).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LocalDate[] dates = new LocalDate[15];
        LocalDate today = LocalDate.now();
        for(int i = 14; i >= 0; i--) {
            dates[i] = today.plusDays(i - 14);
        }

        List<User> newUser = lambdaQuery().between(User::getCreatedAt, begin, end).list();
        Long totalUser = lambdaQuery().count();

        Map<LocalDate, Long> newUserCountMap = newUser.stream()
                .collect(Collectors.groupingBy(user -> user.getCreatedAt().toLocalDate(), Collectors.counting()));

        GetUserDataVO vo = new GetUserDataVO();
        List<Long> totalUserCount = new ArrayList<>();
        List<Long> newUserCount = new ArrayList<>();
        for(LocalDate date : dates) {
            Long todayNewUser = newUserCountMap.getOrDefault(date, 0L);
            totalUserCount.add(totalUser);
            newUserCount.add(todayNewUser);
            totalUser -= todayNewUser;
        }
        vo.setTotalCounts(CollUtil.reverse(totalUserCount));
        vo.setNewCounts(newUserCount);

        return Result.ok(vo);
    }

    @Override
    public Result<GetUserDataVO> getLast30days() {
        LocalDateTime begin = LocalDateTime.now().plusDays(-29).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LocalDate[] dates = new LocalDate[30];
        LocalDate today = LocalDate.now();
        for(int i = 29; i >= 0; i--) {
            dates[i] = today.plusDays(i - 29);
        }

        List<User> newUser = lambdaQuery().between(User::getCreatedAt, begin, end).list();
        Long totalUser = lambdaQuery().count();

        Map<LocalDate, Long> newUserCountMap = newUser.stream()
                .collect(Collectors.groupingBy(user -> user.getCreatedAt().toLocalDate(), Collectors.counting()));

        GetUserDataVO vo = new GetUserDataVO();
        List<Long> totalUserCount = new ArrayList<>();
        List<Long> newUserCount = new ArrayList<>();
        for(LocalDate date : dates) {
            Long todayNewUser = newUserCountMap.getOrDefault(date, 0L);
            totalUserCount.add(totalUser);
            newUserCount.add(todayNewUser);
            totalUser -= todayNewUser;
        }
        vo.setTotalCounts(CollUtil.reverse(totalUserCount));
        vo.setNewCounts(newUserCount);

        return Result.ok(vo);
    }

}
