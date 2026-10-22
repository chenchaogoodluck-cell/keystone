package com.keystone.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.pagehelper.Page;
import com.keystone.admin.error.ErrorCode;

import java.util.List;

/**
 * ApiResult 构建工具
 */
public final class ApiResultUtils {

    private ApiResultUtils() {
    }

    public static <T> ApiResult<T> buildResult() {
        return buildResult(ErrorCode.SUCCESS);
    }

    public static <T> ApiResult<T> buildResult(T data) {
        ApiResult<T> result = buildResult(ErrorCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static <T> ApiResult<T> buildResult(ErrorCode errorCode) {
        return new ApiResult<>(errorCode);
    }

    public static <T> ApiResult<T> buildResult(int code, String msg) {
        return new ApiResult<>(code, msg);
    }

    /**
     * PageHelper 分页结果 → layui table 结构（data 仅为当前页记录，count 为总数）
     */
    public static <T> ApiPageResult<T> buildPageResult(Page<?> page, List<?> dataList) {
        ApiPageResult<T> result = new ApiPageResult<T>(ErrorCode.SUCCESS.code, ErrorCode.SUCCESS.message, "", page.getTotal());
        result.setData(dataList);
        return result;
    }

    /**
     * MyBatis-Plus 分页结果 → layui table 结构
     */
    public static <T> ApiPageResult<T> buildPageResult(IPage<?> page, List<?> dataList) {
        ApiPageResult<T> result = new ApiPageResult<T>(ErrorCode.SUCCESS.code, ErrorCode.SUCCESS.message, "", page.getTotal());
        result.setData(dataList);
        return result;
    }

    public static <T> ApiPageResult<T> buildErrorPageResult(String msg) {
        return new ApiPageResult<T>(ErrorCode.SYSTEM_ERROR.code, msg, "", 0L);
    }
}