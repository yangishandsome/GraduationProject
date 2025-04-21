package com.yxc.common.utils;

import com.yxc.common.domain.UserInfo;

public class UserContext {
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    /**
     * 保存当前登录用户信息到ThreadLocal
     * @param user 用户信息
     */
    public static void setUser(UserInfo user) {
        tl.set(user);
    }

    /**
     * 获取当前登录用户信息
     * @return 用户Change access modifier
     */
    public static UserInfo getUser() {
        return tl.get();
    }

    /**
     * 移除当前登录用户信息
     */
    public static void removeUser(){
        tl.remove();
    }
}
