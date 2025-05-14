package com.yxc.user.Service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.Result;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.PasswordEncoder;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.AdminService;
import com.yxc.user.config.JwtProperties;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.dto.EditAdminDTO;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.po.Admin;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.LoginVO;
import com.yxc.user.mapper.AdminMapper;
import com.yxc.user.utils.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private JwtTool jwtTool;

    @Override
    public Result<LoginVO> login(String username, String password) {
        Admin admin = lambdaQuery().eq(Admin::getUsername, username)
                .one();
        if (admin == null) {
            return Result.error("管理员账号不存在");
        }
        Boolean matches = PasswordEncoder.matches(admin.getPasswordHash(), password);
        if (!matches) {
            return Result.error("密码错误");
        }
        LoginVO vo = new LoginVO();
        UserInfo userInfo = new UserInfo(admin.getAdminId(), "admin", admin.getPermission());
        String token = jwtTool.createToken(JSONUtil.toJsonStr(userInfo), jwtProperties.getTokenTTL());
        vo.setToken(token);
        vo.setUsername(admin.getUsername());
        vo.setUserId(admin.getAdminId());
        log.info("管理员：{}登录成功，管理员id：{}", vo.getUsername(), vo.getUserId());
        return Result.ok(vo);
    }

    @Override
    public Result<Long> addAdmin(AddAdminDTO addAdminDTO) {
        String username = addAdminDTO.getUsername();
        String password = addAdminDTO.getPassword();
        if (StrUtil.length(username) < 5 || StrUtil.length(password) < 6) {
            return StrUtil.length(username) < 5 ? Result.error("用户名长度不能低于5") : Result.error("密码长度不能低于6");
        }

        UserInfo user = UserContext.getUser();
        if (user.getPermission() <= addAdminDTO.getPermission()) {
            return Result.error("新增的管理员权限不能大于等于自身权限");
        }
        Long count = lambdaQuery().eq(Admin::getUsername, username).count();
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(PasswordEncoder.encode(password));
        admin.setPermission(addAdminDTO.getPermission());
        save(admin);
        return Result.ok(0L);
    }

    @Override
    public Result<Long> edit(EditAdminDTO editAdminDTO) {
        String username = editAdminDTO.getUsername();
        if (StrUtil.length(username) < 5) {
            return Result.error("用户名长度不能低于5");
        }

        List<Admin> admins = lambdaQuery().eq(Admin::getUsername, username).list();
        if(!admins.isEmpty() && !Objects.equals(admins.get(0).getAdminId(), editAdminDTO.getAdminId())) {
            return Result.error("用户名已存在");
        }

        UserInfo userInfo = UserContext.getUser();
        if(userInfo.getPermission() <= editAdminDTO.getPermission()) {
            return Result.error("设置的权限不能大于等于自身");
        }

        Admin admin = getById(editAdminDTO.getAdminId());

        if(userInfo.getPermission() <= admin.getPermission()) {
            return Result.error("不能对权限大于等于自身的管理员进行编辑");
        }

        admin.setUsername(username);
        admin.setPermission(editAdminDTO.getPermission());
        updateById(admin);

        return Result.ok(1L);
    }

    @Override
    public Result<Long> deleteAdmin(Long id) {
        Admin admin = getById(id);
        UserInfo userInfo = UserContext.getUser();
        if(userInfo.getUserId().equals(id)) {
            return Result.error("无法删除自身");
        }
        if (userInfo.getPermission() <= admin.getPermission()) {
            return Result.error("无法删除权限大于等于自身权限的管理员");
        }
        removeById(id);
        return Result.ok(1L);
    }
}
