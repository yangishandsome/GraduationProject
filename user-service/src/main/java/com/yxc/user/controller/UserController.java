package com.yxc.user.controller;

import cn.hutool.core.util.StrUtil;
import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.UserService;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.dto.RegisterDTO;
import com.yxc.user.domain.dto.RegisterVerifyDTO;
import com.yxc.user.domain.dto.UpdateUserInfoDTO;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    private Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if(StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
            return Result.error("用户名或密码错误");
        }
        return userService.login(username, password);
    }

    @GetMapping("/sendCode")
    private Result<Long> sendCode(@RequestParam String phone) {
        return userService.sendCode(phone);
    }

    @GetMapping("/randomUsername")
    private Result<String> randomUsername() {
        return userService.randomUsername();
    }

    @PostMapping("/register")
    private Result<RegisterVO> register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PostMapping("/registerVerify")
    private Result<RegisterVerifyVO> registerVerify(@RequestBody RegisterVerifyDTO registerVerifyDTO) {
        return userService.registerVerify(registerVerifyDTO);
    }

    @PostMapping("/updateUserInfo")
    private Result<Long> updateUserInfo(@RequestBody UpdateUserInfoDTO updateUserInfoDTO) {
        return userService.updateUserInfo(updateUserInfoDTO);
    }

    @GetMapping("/getUserInfo")
    private Result<UserInfoVO> getUserInfo() {
        Long userId = UserContext.getUser().getUserId();
        return userService.getUserInfo(userId);
    }

    @GetMapping("/getTodayUserData")
    private Result<TodayUserDataVO> getTodayUserData() {
        return userService.getTodayUserData();
    }

    @GetMapping("/getUserData/last7days")
    private Result<GetUserDataVO> getLast7days() {
        return userService.getLast7days();
    }

    @GetMapping("/getUserData/last15days")
    private Result<GetUserDataVO> getLast15days() {
        return userService.getLast15days();
    }

    @GetMapping("/getUserData/last30days")
    private Result<GetUserDataVO> getLast30days() {
        return userService.getLast30days();
    }

    /**
     * OpenFeign接口
     */
    @GetMapping("/getUserByIds")
    private List<User> getUserByIds(@RequestParam("ids") List<Long> ids) {
        return userService.getUserByIds(ids);
    }

}
