package com.keystone.admin.exception;

import lombok.Data;

/**
 * 业务异常
 */
@Data
public class BusinessException extends RuntimeException {

    private final int errCode;
    private String msg;

    public BusinessException(int code, String msg) {
        super(msg);
        this.errCode = code;
        this.msg = msg;
    }
}