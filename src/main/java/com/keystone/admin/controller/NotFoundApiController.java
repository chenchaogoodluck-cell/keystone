package com.keystone.admin.controller;

import com.keystone.admin.error.ErrorCode;
import com.keystone.admin.web.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * API 兜底：匹配所有未被具体 handler 处理的 /api/** 请求，统一返回 4003 JSON。
 * <p>
 * 取代对 {@code NoHandlerFoundException} + {@code spring.mvc.throw-exception-if-no-handler-found}
 * 的依赖（后者在开启静态资源映射时不会可靠触发），保证不存在的接口总是返回统一结构。
 * 仍会被登录拦截器拦截（/api/**），因此未登录请求到此或未命中会先返回 1001。
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class NotFoundApiController {

    @RequestMapping("/**")
    public ApiResult<Void> notFound(HttpServletRequest request) {
        log.info("Keystone API 未找到对应处理，method={}, uri={}", request.getMethod(), request.getRequestURI());
        return new ApiResult<>(ErrorCode.HTTP_URL_NO_HANDLER_ERROR);
    }
}