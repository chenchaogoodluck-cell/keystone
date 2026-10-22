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
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 统一异常处理：所有接口返回统一 {@link ApiResult} 结构
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** URL 不存在 */
    @ExceptionHandler(value = {NoHandlerFoundException.class})
    public <T> ApiResult<T> noHandlerFoundException(HttpServletRequest request, NoHandlerFoundException ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.HTTP_URL_NO_HANDLER_ERROR);
        printErrorLog(request, ex);
        return result;
    }

    /** 通用异常 */
    @ExceptionHandler(value = Exception.class)
    public <T> ApiResult<T> handleException(HttpServletRequest request, Exception ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.SYSTEM_ERROR);
        printErrorLog(request, ex);
        return result;
    }

    /** 业务异常 */
    @ExceptionHandler(value = BusinessException.class)
    public <T> ApiResult<T> handleBusinessException(HttpServletRequest request, BusinessException ex) {
        ApiResult<T> result = new ApiResult<>(ex.getErrCode(), ex.getMessage());
        printErrorLog(request, ex);
        return result;
    }

    /** 参数异常（RequestParam 校验 / 参数不合法） */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public <T> ApiResult<T> handleBindException(HttpServletRequest request, Exception ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR.code, ex.getMessage());
        printErrorLog(request, ex);
        return result;
    }

    /** 参数绑定 / 转换异常 */
    @ExceptionHandler(value = BindException.class)
    public <T> ApiResult<T> handleBindException(HttpServletRequest request, BindException ex) {
        ApiResult<T> result = new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR);
        printErrorLog(request, ex);
        BindingResult exceptions = ex.getBindingResult();
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty() && errors.get(0) instanceof FieldError) {
                return new ApiResult<>(ErrorCode.PARAM_INVALID_ERROR.code, ((FieldError) errors.get(0)).getDefaultMessage());
            }
        }
        return result;
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

    private void printErrorLog(HttpServletRequest request, Exception ex) {
        log.error("Keystone 统一异常处理 method={}, url={}", request.getMethod(), request.getRequestURI(), ex);
    }
}