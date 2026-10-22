package com.keystone.admin.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 错误码定义
 */
@AllArgsConstructor
public class ErrorCode {

    @Getter
    public final Integer code;

    public final String message;

    /** 成功 */
    public static final ErrorCode SUCCESS = new ErrorCode(0, "请求成功");

    /** 系统错误（未知异常统一错误） */
    public static final ErrorCode SYSTEM_ERROR = new ErrorCode(5000, "系统异常");

    /** 请求参数非法 */
    public static final ErrorCode PARAM_INVALID_ERROR = new ErrorCode(4000, "请求参数非法");

    /** 请求方法不支持 */
    public static final ErrorCode HTTP_METHOD_NOT_SUPPORT_ERROR = new ErrorCode(4001, "请求方法不支持");

    /** 请求体错误 */
    public static final ErrorCode HTTP_MESSAGE_NOT_READABLE_ERROR = new ErrorCode(4002, "请求体错误");

    /** 请求的资源未找到 */
    public static final ErrorCode HTTP_URL_NO_HANDLER_ERROR = new ErrorCode(4003, "请求的资源未找到");

    /** 未登录或登录失效（与 static/config.js 中 setter.response.statusCode.logout 保持一致） */
    public static final ErrorCode LOGIN_INVALID_ERROR = new ErrorCode(1001, "未登录或登录已过期");

    /** 用户无权限 */
    public static final ErrorCode USER_FORBIDDEN_ERROR = new ErrorCode(4010, "用户无权限");

    public String getMessage() {
        return message;
    }

    /** 自定义错误码 */
    public static ErrorCode fail(Integer code, String message) {
        return new ErrorCode(code, message);
    }

    /** 消息格式化 */
    public ErrorCode formatMessage(Object... args) {
        return new ErrorCode(this.code, MessageFormat.format(message, args));
    }
}