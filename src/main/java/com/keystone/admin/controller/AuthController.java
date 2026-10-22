package com.keystone.admin.controller;

import com.keystone.admin.service.AuthService;
import com.keystone.admin.utils.JwtUtils;
import com.keystone.admin.vo.LoginReqVo;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 认证接口：登录
 */
@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    /** token Cookie 有效期 30 天（与 createManagementToken 一致） */
    private static final int TOKEN_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

    @Resource
    private AuthService authService;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @RequestMapping("/login")
    public ApiResult<Map<String, String>> login(@Validated @RequestBody LoginReqVo reqVo, HttpServletResponse response) {
        Map<String, String> result = authService.login(reqVo);
        // 登录后把 token 写入 HttpOnly Cookie：iframe 弹层 / 内部跳转等无法携带请求头时由服务端从 Cookie 读取
        addTokenCookie(response, result.get("token"));
        return ApiResultUtils.buildResult(result);
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        if (response == null || token == null || token.trim().isEmpty()) {
            return;
        }
        response.addHeader("Set-Cookie",
                JwtUtils.TOKEN_COOKIE + "=" + token
                        + "; Path=/"
                        + "; Max-Age=" + TOKEN_COOKIE_MAX_AGE_SECONDS
                        + "; HttpOnly"
                        + (cookieSecure ? "; Secure" : "")
                        + "; SameSite=Lax");
        log.info("Keystone 登录成功，token 已写入 HttpOnly Cookie");
    }
}