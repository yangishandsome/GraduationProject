package com.yxc.user.controller;

import com.yxc.common.domain.Result;
import com.yxc.user.Service.AdminService;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @GetMapping("/login")
    private Result<LoginVO> login(@RequestParam(value = "username") String username,
                                  @RequestParam(value = "password") String password) {
        return adminService.login(username, password);
    }

    @PostMapping("/addAdmin")
    private Result<Long> addAdmin(@RequestBody AddAdminDTO addAdminDTO) {
        return adminService.addAdmin(addAdminDTO);
    }

}
