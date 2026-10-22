package com.keystone.admin.config;

import com.keystone.admin.dto.CurrentUserDto;

/**
 * 当前登录用户 ThreadLocal（登录拦截器中写入，请求结束后清理）
 */
public class CurrentUserHolder {

    private CurrentUserHolder() {
    }

    private static final ThreadLocal<CurrentUserDto> USER_THREAD_LOCAL = new ThreadLocal<>();

    public static CurrentUserDto get() {
        return USER_THREAD_LOCAL.get();
    }

    public static void set(CurrentUserDto currentUser) {
        USER_THREAD_LOCAL.set(currentUser);
    }

    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }

    /** 获取当前登录用户名 */
    public static String getName() {
        CurrentUserDto user = get();
        return user == null ? null : user.getName();
    }
}