package com.yxc.user.controller;

import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.UserService;
import com.yxc.user.domain.dto.RegisterDTO;
import com.yxc.user.domain.dto.RegisterVerifyDTO;
import com.yxc.user.domain.dto.UpdateUserInfoDTO;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.LoginVO;
import com.yxc.user.domain.vo.RegisterVO;
import com.yxc.user.domain.vo.RegisterVerifyVO;
import com.yxc.user.domain.vo.UserInfoVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/login")
    private Result<LoginVO> login(@RequestParam(value = "username") String username,
                                  @RequestParam(value = "password") String password) {
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
        Long userId = UserContext.getUser();
        return userService.getUserInfo(userId);
    }

    /**
     * OpenFeign接口
     */
    @GetMapping("/getUserByIds")
    private List<User> getUserByIds(@RequestParam("ids") List<Long> ids) {
        return userService.getUserByIds(ids);
    }

}
