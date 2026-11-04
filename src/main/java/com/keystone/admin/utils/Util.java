package com.keystone.admin.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.keystone.admin.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 通用工具
 */
public final class Util {

    private Util() {
    }

    /** 默认当前页 */
    public static final int DEFAULT_PAGE = 1;
    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 15;
    /** 单次分页最大条数：防止前端传超大 limit 一次拉全表打垮 DB/内存 */
    public static final int MAX_PAGE_SIZE = 200;

    /** String 转 List */
    public static <E> List<E> getStrToList(String str, Class<E> clazz) {
        return JSONObject.parseArray(str, clazz);
    }

    /** 归一化当前页：小于 1 时回退默认第 1 页 */
    public static int normalizePage(int page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    /** 归一化每页条数：小于 1 回退默认 15，超过上限回退 {@link #MAX_PAGE_SIZE} */
    public static int normalizeLimit(int limit) {
        if (limit < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        if (limit > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return limit;
    }

    /** 根据请求携带的 token 获取当前登录用户名（校验签名与过期，防止伪造 token 解出用户名） */
    public static String getNameByToken(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            return JwtUtils.parseAndVerifyToken(token).getAudience().get(0);
        } catch (Exception e) {
            return null;
        }
    }
}