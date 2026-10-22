package com.keystone.admin.web;

import com.keystone.admin.error.ErrorCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用接口返回封装
 */
@Getter
@Setter
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private String traceId;
    private T data;

    public ApiResult(Integer code, String message) {
        this(code, message, "");
    }

    public ApiResult(Integer code, String message, String traceId) {
        this.code = code;
        this.msg = message;
        this.traceId = traceId;
    }

    public ApiResult(ErrorCode errorCode) {
        this(errorCode.code, errorCode.getMessage());
    }

    public boolean success() {
        return ErrorCode.SUCCESS.code.equals(this.code);
    }
}