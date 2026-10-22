package com.keystone.admin.utils;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 通用工具
 */
public final class Util {

    private Util() {
    }

    /** String 转 List */
    public static <E> List<E> getStrToList(String str, Class<E> clazz) {
        return JSONObject.parseArray(str, clazz);
    }

    /** 根据请求携带的 token 获取当前登录用户名 */
    public static String getNameByToken(HttpServletRequest request) {
        return JwtUtils.getAudience(JwtUtils.resolveToken(request));
    }
}