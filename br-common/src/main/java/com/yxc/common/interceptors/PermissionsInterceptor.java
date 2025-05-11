package com.yxc.common.interceptors;


import com.yxc.common.domain.UserInfo;
import com.yxc.common.exception.BizIllegalException;
import com.yxc.common.utils.UserContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@AllArgsConstructor
public class PermissionsInterceptor implements HandlerInterceptor {

    private final Integer permission;

    private final String[] decoration = new String[]{"查看", "编辑", "新增", "删除"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo user = UserContext.getUser();
        log.info("用户权限：{}", user.getPermission());
        if(user.getPermission() < permission) {
            response.setStatus(403);
            return false;
        }
        return true;
    }
}
