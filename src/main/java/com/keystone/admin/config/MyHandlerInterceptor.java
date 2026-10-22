package com.keystone.admin.config;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.keystone.admin.dto.CurrentUserDto;
import com.keystone.admin.error.ErrorCode;
import com.keystone.admin.exception.BusinessException;
import com.keystone.admin.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录拦截器（仅拦截 /api/** 接口，token 从请求头 x-keystone-auth-token 或 HttpOnly Cookie 获取）
 * <p>未登录 / token 无效时返回 code=1001，前端根据该码跳转登录页。</p>
 */
@Slf4j
public class MyHandlerInterceptor implements HandlerInterceptor {

    /** 登录失效状态码，与 static/config.js 中 setter.response.statusCode.logout 保持一致 */
    private static final int LOGOUT_CODE = ErrorCode.LOGIN_INVALID_ERROR.code;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = JwtUtils.resolveToken(request);
        if (token == null || token.trim().isEmpty()) {
            log.info("Keystone 未登录，访问地址：{}", request.getRequestURI());
            writeUnLogin(response);
            return false;
        }
        try {
            DecodedJWT jwt = JwtUtils.parseAndVerifyToken(token);
            String userName = jwt.getAudience().get(0);
            CurrentUserDto userDto = new CurrentUserDto();
            userDto.setId(userName);
            userDto.setName(userName);
            CurrentUserHolder.set(userDto);
            return true;
        } catch (BusinessException e) {
            log.warn("Keystone token 无效或已过期，访问地址：{}，原因：{}", request.getRequestURI(), e.getMessage());
            writeUnLogin(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal，防止线程池复用导致用户串号或内存泄漏
        CurrentUserHolder.remove();
    }

    private void writeUnLogin(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject json = new JSONObject();
        json.put("code", LOGOUT_CODE);
        json.put("msg", "未登录或登录已过期");
        response.getWriter().write(json.toJSONString());
    }
}