package com.yxc.user.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.yxc.common.domain.Result;
import com.yxc.user.Service.AdminService;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.dto.EditAdminDTO;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.vo.AdminInfoVO;
import com.yxc.user.domain.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @GetMapping("/list")
    private Result<List<AdminInfoVO>> list() {
        return Result.ok(BeanUtil.copyToList(adminService.list(), AdminInfoVO.class));
    }

    @PutMapping("/addAdmin")
    private Result<Long> addAdmin(@RequestBody AddAdminDTO addAdminDTO) {
        return adminService.addAdmin(addAdminDTO);
    }

    @PostMapping("/edit")
    private Result<Long> edit(@RequestBody EditAdminDTO editAdminDTO) {
        return adminService.edit(editAdminDTO);
    }

    @DeleteMapping("/deleteAdmin/{id}")
    private Result<Long> deleteAdmin(@PathVariable(value = "id") Long id) {
        return adminService.deleteAdmin(id);
    }

}
