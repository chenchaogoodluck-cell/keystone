package com.keystone.admin.controller;

import com.keystone.admin.service.AuthService;
import com.keystone.admin.utils.JwtUtils;
import com.keystone.admin.vo.LoginReqVo;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Resource
    private AuthService authService;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ApiResult<Map<String, String>> login(@Validated @RequestBody LoginReqVo reqVo, HttpServletResponse response) {
        Map<String, String> result = authService.login(reqVo);
        // 浏览器端只依赖 Cookie 鉴权：token 不再交给前端保存，避免 XSS 从 localStorage 窃取；
        // iframe 弹层 / 内部跳转等同源请求也会自动携带该 Cookie。
        addAuthCookies(response, result.get("token"));
        return ApiResultUtils.buildResult(result);
    }

    private void addAuthCookies(HttpServletResponse response, String token) {
        if (response == null || token == null || token.trim().isEmpty()) {
            return;
        }
        String suffix = "; Path=/; Max-Age=" + JwtUtils.TOKEN_MAX_AGE_SECONDS
                + "; SameSite=Lax" + (cookieSecure ? "; Secure" : "");
        // 登录 token：HttpOnly，JS 无法读取
        response.addHeader("Set-Cookie", JwtUtils.TOKEN_COOKIE + "=" + token + suffix + "; HttpOnly");
        // CSRF token：非 HttpOnly，前端读取后放进自定义请求头；值由 JWT 密钥对当前 token 签名，
        // 服务端无状态校验（见 BrowserSecurityFilter），补上 Cookie 鉴权下的跨站写请求防护。
        response.addHeader("Set-Cookie", JwtUtils.buildCsrfCookie(token, cookieSecure));
        log.info("Keystone 登录成功，已写入 HttpOnly token Cookie 与 CSRF Cookie");
    }
}
