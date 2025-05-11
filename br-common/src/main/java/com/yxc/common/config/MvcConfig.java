package com.yxc.common.config;

import com.yxc.common.interceptors.PermissionsInterceptor;
import com.yxc.common.interceptors.UserInfoInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
@ConditionalOnClass(DispatcherServlet.class)
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private PermissionProperties permissionProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInfoInterceptor()).addPathPatterns("/**").order(0);
        registry.addInterceptor(new PermissionsInterceptor(1)).addPathPatterns(permissionProperties.getLevel1Paths()).order(1);
        registry.addInterceptor(new PermissionsInterceptor(2)).addPathPatterns(permissionProperties.getLevel2Paths()).order(2);
        registry.addInterceptor(new PermissionsInterceptor(3)).addPathPatterns(permissionProperties.getLevel3Paths()).order(3);
    }
}
