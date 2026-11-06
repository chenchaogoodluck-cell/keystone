package com.keystone.admin.exception;

import com.keystone.admin.error.ErrorCode;
import com.keystone.admin.web.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 统一异常处理：所有接口返回统一 {@link ApiResult} 结构
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* URL 不存在：由 NotFoundApiController 的 /api/** 兜底统一返回 4003（此处不再处理
     * NoHandlerFoundException，避免依赖 throw-exception-if-no-handler-found 开关的不确定性）。 */

    /** 通用异常（未知错误，上报 ERROR，返回统一 5000） */
    @ExceptionHandler(value = Exception.class)
    public <T> ApiResult<T> handleException(HttpServletRequest request, Exception ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.SYSTEM_ERROR);
        printErrorLog(request, ex);
        return result;
    }

    /** 业务异常（预期内的异常，降级为 WARN，避免刷屏淹没真实错误） */
    @ExceptionHandler(value = BusinessException.class)
    public <T> ApiResult<T> handleBusinessException(HttpServletRequest request, BusinessException ex) {
        ApiResult<T> result = new ApiResult<>(ex.getErrCode(), ex.getMessage());
        log.warn("Keystone 业务异常 method={}, url={}, msg={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return result;
    }

    /** @Validated / @Valid 请求体校验失败：优先返回具体字段的校验信息 */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public <T> ApiResult<T> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException ex) {
        String msg = resolveFieldError(ex.getBindingResult());
        log.warn("Keystone 参数校验失败 method={}, url={}, msg={}", request.getMethod(), request.getRequestURI(), msg);
        return new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR.code, msg);
    }

    /** 非法参数（如非法枚举值 / 校验断言失败） */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public <T> ApiResult<T> handleIllegalArgument(HttpServletRequest request, IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.PARAM_INVALID_ERROR.getMessage();
        log.warn("Keystone 非法参数 method={}, url={}, msg={}", request.getMethod(), request.getRequestURI(), msg);
        return new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR.code, msg);
    }

    /** 参数绑定 / 类型转换异常 */
    @ExceptionHandler(value = BindException.class)
    public <T> ApiResult<T> handleBindException(HttpServletRequest request, BindException ex) {
        String msg = resolveFieldError(ex.getBindingResult());
        log.warn("Keystone 参数绑定异常 method={}, url={}, msg={}", request.getMethod(), request.getRequestURI(), msg);
        return new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR.code, msg);
    }

    /** 参数缺失 */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public <T> ApiResult<T> handleMissingServletRequestParameterException(HttpServletRequest request, MissingServletRequestParameterException ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR);
        printErrorLog(request, ex);
        return result;
    }

    /** 请求方法不支持 */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public <T> ApiResult<T> handleMethodNotSupportException(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.HTTP_METHOD_NOT_SUPPORT_ERROR.code, ex.getMessage());
        printErrorLog(request, ex);
        return result;
    }

    /** 请求体为空 / 不可读 */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public <T> ApiResult<T> handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.HTTP_MESSAGE_NOT_READABLE_ERROR);
        printErrorLog(request, ex);
        return result;
    }

    private String resolveFieldError(BindingResult bindingResult) {
        if (bindingResult != null && bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty() && errors.get(0) instanceof FieldError) {
                String defaultMessage = ((FieldError) errors.get(0)).getDefaultMessage();
                if (defaultMessage != null) {
                    return defaultMessage;
                }
            }
        }
        return ErrorCode.PARAM_INVALID_ERROR.getMessage();
    }

    private void printErrorLog(HttpServletRequest request, Exception ex) {
        log.error("Keystone 统一异常处理 method={}, url={}", request.getMethod(), request.getRequestURI(), ex);
    }
}