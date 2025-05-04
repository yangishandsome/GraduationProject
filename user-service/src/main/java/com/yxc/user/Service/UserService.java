package com.yxc.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.Result;
import com.yxc.user.domain.dto.RegisterDTO;
import com.yxc.user.domain.dto.RegisterVerifyDTO;
import com.yxc.user.domain.dto.UpdateUserInfoDTO;
import com.yxc.user.domain.po.User;
import com.yxc.user.domain.vo.*;

import java.util.List;

public interface UserService extends IService<User> {
    Result<LoginVO> login(String username, String password);

    Result<Long> sendCode(String phone);

    Result<String> randomUsername();

    Result<RegisterVO> register(RegisterDTO registerDTO);

    Result<RegisterVerifyVO> registerVerify(RegisterVerifyDTO registerVerifyDTO);

    Result<UserInfoVO> getUserInfo(Long userId);

    Result<Long> updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO);

    List<User> getUserByIds(List<Long> ids);

    Result<TodayUserDataVO> getTodayUserData();

    Result<GetUserDataVO> getLast7days();

    Result<GetUserDataVO> getLast15days();

    Result<GetUserDataVO> getLast30days();
}
