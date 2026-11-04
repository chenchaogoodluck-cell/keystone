package com.keystone.admin.controller;

import com.keystone.admin.config.CurrentUserHolder;
import com.keystone.admin.utils.JwtUtils;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前用户接口：退出登录 / 个人资料
 */
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Resource
    private JwtUtils jwtUtils;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    /** 退出登录：删除缓存的 token，并让 HttpOnly Cookie 立即过期（否则旧 Cookie 会残留至 30 天 Max-Age） */
    @PostMapping("/logout")
    public ApiResult<Void> logout(HttpServletResponse response) {
        String username = CurrentUserHolder.getName();
        jwtUtils.invalidateToken(username);
        // 写回同名空值 + Max-Age=0 的 Cookie，指示浏览器立即删除，避免登出后残留失效 token
        response.addHeader("Set-Cookie",
                JwtUtils.TOKEN_COOKIE + "=; Path=/; Max-Age=0; HttpOnly"
                        + (cookieSecure ? "; Secure" : "") + "; SameSite=Lax");
        log.info("Keystone 用户退出登录：username={}", username);
        return ApiResultUtils.buildResult();
    }

    /** 当前登录用户信息（个人资料页使用） */
    @RequestMapping("/getInfo")
    public ApiResult<Map<String, Object>> getInfo() {
        String username = CurrentUserHolder.getName();
        Map<String, Object> info = new HashMap<>(6);
        info.put("username", username);
        info.put("name", username);
        info.put("title", "Keystone 用户");
        info.put("email", "");
        info.put("phoneNumber", "");
        info.put("sex", "");
        info.put("hiredDate", "");
        info.put("workPlace", "暂无");
        return ApiResultUtils.buildResult(info);
    }
}