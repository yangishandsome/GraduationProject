package com.yxc.user.Service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.Result;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.PasswordEncoder;
import com.yxc.user.Service.AdminService;
import com.yxc.user.config.JwtProperties;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.po.Admin;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.LoginVO;
import com.yxc.user.mapper.AdminMapper;
import com.yxc.user.utils.JwtTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
        UserInfo userInfo = new UserInfo(admin.getAdminId(), "admin");
        String token = jwtTool.createToken(JSONUtil.toJsonStr(userInfo), jwtProperties.getTokenTTL());
        vo.setToken(token);
        vo.setUsername(admin.getUsername());
        vo.setUserId(admin.getAdminId());
        log.info("管理员：{}登录成功，管理员id：{}", vo.getUsername(), vo.getUserId());
        return Result.ok(vo);
    }

    @Override
    public Result<Long> addAdmin(AddAdminDTO addAdminDTO) {
        if(StrUtil.isEmpty(addAdminDTO.getUsername()) || StrUtil.isEmpty(addAdminDTO.getPassword())) {
            return StrUtil.isEmpty(addAdminDTO.getUsername())? Result.error("用户名不能为空") : Result.error("密码不能为空");
        }
        Admin admin = new Admin();
        admin.setUsername(addAdminDTO.getUsername());
        admin.setPasswordHash(PasswordEncoder.encode(addAdminDTO.getPassword()));
        admin.setAuthorization(addAdminDTO.getAuthorization());
        save(admin);
        return Result.ok(0L);
    }
}
