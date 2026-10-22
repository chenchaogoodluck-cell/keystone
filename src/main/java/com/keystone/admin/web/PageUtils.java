package com.keystone.admin.web;

import com.github.pagehelper.Page;
import com.keystone.admin.constant.CommonConstants;

/**
 * 分页工具
 */
public final class PageUtils {

    private PageUtils() {
    }

    /**
     * 复制 PageHelper 分页属性到自定义返回对象
     */
    public static void copyPageProps2Resp(Page page, long[] pageInfo) {
        pageInfo[0] = page.getPageNum();
        pageInfo[1] = page.getPageSize();
        pageInfo[2] = page.getPages();
        pageInfo[3] = page.getTotal();
    }

    /**
     * 简化：由 total 与 pageSize 计算总页数，并填充到返回对象
     */
    public static int calcPageCount(Integer total, Integer pageSize) {
        if (pageSize == null || pageSize <= 0 || total == null || total <= 0) {
            return CommonConstants.INT_0;
        }
        return (int) (total / pageSize + (total % pageSize == 0 ? CommonConstants.INT_0 : CommonConstants.INT_1));
    }
}