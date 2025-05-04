package com.yxc.user.controller;

import cn.hutool.core.util.StrUtil;
import com.yxc.common.domain.Result;
import com.yxc.user.Service.AdminService;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    private Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if(StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
            return Result.error("用户名或密码错误");
        }
        return adminService.login(username, password);
    }

    @PostMapping("/addAdmin")
    private Result<Long> addAdmin(@RequestBody AddAdminDTO addAdminDTO) {
        return adminService.addAdmin(addAdminDTO);
    }

}
