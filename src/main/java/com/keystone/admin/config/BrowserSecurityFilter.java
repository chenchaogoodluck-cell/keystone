package com.keystone.admin.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.keystone.admin.error.ErrorCode;
import com.keystone.admin.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 浏览器侧安全过滤器：安全响应头（CSP 等） + Cookie 鉴权下的 CSRF 双提交校验。
 * <p>
 * layuiAdmin 的模板引擎依赖 {@code laytpl} / {@code new Function}，页面也存在内联脚本，因此
 * CSP 必须放开 {@code 'unsafe-inline'} / {@code 'unsafe-eval'}；其收敛点在于：禁止加载外部脚本、
 * 禁止向外部地址发起请求（{@code connect-src 'self'}，XSS 即使得手也难以外传数据）、禁止被外站嵌套。
 * 若要进一步收紧，需先把模板改为预编译并移除内联脚本。
 * </p>
 * <p>
 * CSRF：登录 token 存 HttpOnly Cookie 后，写请求不再有「浏览器无法自动伪造的自定义请求头」，
 * 故要求前端把非 HttpOnly 的 CSRF Cookie 值回传到同名请求头，二者均须等于
 * {@code HMAC(jwtSecret, token)}。显式携带 token 请求头的是非浏览器 API 客户端，
 * 该请求头不会被浏览器自动附带，不构成 CSRF，因此放行。
 * </p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BrowserSecurityFilter extends OncePerRequestFilter {

    private static final String CSP = "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' 'unsafe-eval'; "
            + "style-src 'self' 'unsafe-inline'; "
            + "img-src 'self' data:; "
            + "font-src 'self' data:; "
            + "connect-src 'self'; "
            + "frame-src 'self'; "
            + "frame-ancestors 'self'; "
            + "object-src 'none'; "
            + "base-uri 'self'; "
            + "form-action 'self'";

    /** 登录时还没有 token / CSRF Cookie，登录接口本身也不改变已登录用户的数据 */
    private static final String LOGIN_PATH = "/api/v1/auth/login";

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        applySecurityHeaders(response);
        ensureCsrfCookie(request, response);
        if (requiresCsrf(request) && !csrfValid(request)) {
            log.warn("Keystone CSRF 校验失败 method={}, url={}", request.getMethod(), request.getRequestURI());
            writeCsrfError(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void applySecurityHeaders(HttpServletResponse response) {
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("X-Content-Type-Options", "nosniff");
        // layuiAdmin iframe 版与 Druid 监控页都用同源 iframe，SAMEORIGIN 允许同源嵌入、拒绝外站嵌套
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    /**
     * 升级兼容：改造前已登录的会话只有 token Cookie、没有 CSRF Cookie，写请求会被 4004 全部拦下。
     * 检测到有效 token 但缺 CSRF Cookie 时补发一次，用户刷新页面即可继续操作，无需强制全体重新登录。
     */
    private void ensureCsrfCookie(HttpServletRequest request, HttpServletResponse response) {
        if (readCookie(request, JwtUtils.CSRF_COOKIE) != null) {
            return;
        }
        String token = JwtUtils.resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return;
        }
        try {
            JwtUtils.parseAndVerifyToken(token);
        } catch (Exception e) {
            return; // 无效 / 过期 token 交给登录拦截器返回 1001
        }
        response.addHeader("Set-Cookie", JwtUtils.buildCsrfCookie(token, cookieSecure));
    }

    /** 仅校验 /api/** 下会改变状态的请求；GET/HEAD/OPTIONS/TRACE 与登录接口放行 */
    private boolean requiresCsrf(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method) || "TRACE".equalsIgnoreCase(method)) {
            return false;
        }
        String path = request.getServletPath();
        return path != null && path.startsWith("/api/") && !LOGIN_PATH.equals(path);
    }

    private boolean csrfValid(HttpServletRequest request) {
        if (StrUtil.isNotBlank(request.getHeader(JwtUtils.TOKEN_HEADER))) {
            return true;
        }
        String token = JwtUtils.resolveToken(request);
        return JwtUtils.isCsrfTokenValid(readCookie(request, JwtUtils.CSRF_COOKIE),
                request.getHeader(JwtUtils.CSRF_HEADER), token);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /** 与登录拦截器一致：返回 HTTP 200 + 业务码，前端 success 分支才能展示具体提示 */
    private void writeCsrfError(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSONObject json = new JSONObject();
        json.put("code", ErrorCode.CSRF_INVALID_ERROR.code);
        json.put("msg", ErrorCode.CSRF_INVALID_ERROR.getMessage());
        response.getWriter().write(json.toJSONString());
    }
}
