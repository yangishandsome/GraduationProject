package com.yxc.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.Result;
import com.yxc.user.domain.dto.AddAdminDTO;
import com.yxc.user.domain.dto.LoginDTO;
import com.yxc.user.domain.po.Admin;
import com.yxc.user.domain.vo.LoginVO;

public interface AdminService extends IService<Admin> {
    Result<LoginVO> login(String username, String password);

    Result<Long> addAdmin(AddAdminDTO addAdminDTO);
}
