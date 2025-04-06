package com.yxc.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.Result;
import com.yxc.user.domain.dto.RegisterDTO;
import com.yxc.user.domain.dto.RegisterVerifyDTO;
import com.yxc.user.domain.dto.UpdateUserInfoDTO;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.LoginVO;
import com.yxc.user.domain.vo.RegisterVO;
import com.yxc.user.domain.vo.RegisterVerifyVO;
import com.yxc.user.domain.vo.UserInfoVO;

public interface UserService extends IService<User> {
    Result<LoginVO> login(String username, String password);

    Result<Long> sendCode(String phone);

    Result<String> randomUsername();

    Result<RegisterVO> register(RegisterDTO registerDTO);

    Result<RegisterVerifyVO> registerVerify(RegisterVerifyDTO registerVerifyDTO);

    Result<UserInfoVO> getUserInfo(Long userId);

    Result<Long> updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO);
}
