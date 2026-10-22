package com.keystone.admin.web;

import com.keystone.admin.error.ErrorCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页接口返回封装（layui table 约定：code + msg + count + data）
 */
@Setter
@Getter
public class ApiPageResult<T> {
    private Integer code;
    private String msg;
    private String traceId;
    private Object data;
    private Long count;

    public ApiPageResult(Integer code, String message) {
        this(code, message, "", 0L);
    }

    public ApiPageResult(Integer code, String message, String traceId, Long count) {
        this.code = code;
        this.msg = message;
        this.traceId = traceId;
        this.count = count;
    }

    public ApiPageResult(ErrorCode errorCode) {
        this(errorCode.code, errorCode.getMessage());
    }

    public boolean success() {
        return ErrorCode.SUCCESS.code.equals(this.code);
    }
}