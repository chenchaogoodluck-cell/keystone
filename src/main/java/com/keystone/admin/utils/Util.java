package com.keystone.admin.utils;

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
}