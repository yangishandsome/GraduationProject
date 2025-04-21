package com.yxc.common.interceptors;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yxc.common.domain.UserInfo;
import com.yxc.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userJson = request.getHeader("user-info");
        if(StrUtil.isNotBlank(userJson)) {
            UserInfo userInfo = JSONUtil.toBean(userJson, UserInfo.class);
            UserContext.setUser(userInfo);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
