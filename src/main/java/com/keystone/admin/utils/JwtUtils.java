package com.keystone.admin.utils;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.keystone.admin.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具：签发 / 校验登录 token
 * <ul>
 *     <li>浏览器端登录后只把 token 写入同名 HttpOnly Cookie（前端不再保存 token，避免 XSS 从
 *         localStorage 窃取）；非浏览器 API 客户端仍可显式用请求头 {@link #TOKEN_HEADER} 携带 token；</li>
 *     <li>Cookie 鉴权的写请求由 {@code BrowserSecurityFilter} 做 CSRF 双提交校验，
 *         本类提供与登录 token 绑定的 CSRF token 生成 / 校验；</li>
 *     <li>签名密钥来自 {@code app.jwt.secret}（环境变量 JWT_SECRET），未配置则启动失败（fail-fast）；</li>
 *     <li>已签发 token 缓存在内存（按 profile 隔离），支持登录态管理（退出时删除）。
 *         当前为无 Redis 的内存实现；如需 Redis 分发/失效，替换为注入 RedisTemplate 缓存 token 即可。</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtUtils {

    /** 内存 token 缓存：key=prefix+username，value=token（进程内有效，重启即失效）
     *  static 以便鉴权拦截器在签名/过期校验之外，再核对 token 是否仍是该用户当前有效的
     *  登录 token（登出后从缓存移除，即实现「log-out 后真正失效」）。 */
    private static final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /** 登录 token 的请求头 / HttpOnly Cookie 字段名（请求头供非浏览器 API 客户端使用） */
    public static final String TOKEN_HEADER = "x-keystone-auth-token";
    public static final String TOKEN_COOKIE = "x-keystone-auth-token";

    /** CSRF 双提交：非 HttpOnly Cookie 名 + 前端回传的请求头名（须与 static/config.js setter.csrf 一致） */
    public static final String CSRF_COOKIE = "keystone-csrf";
    public static final String CSRF_HEADER = "X-Keystone-Csrf";

    /** 登录 token 与 CSRF Cookie 有效期（30 天，与 createManagementToken 一致） */
    public static final int TOKEN_MAX_AGE_SECONDS = 60 * 60 * 24 * 30;

    /** 服务器端 JWT 签名密钥（从配置注入，未配置则启动失败） */
    private static String jwtSecret;

    private static String tokenPrefix;

    @Value("${app.jwt.secret:}")
    public void setJwtSecret(String secret) {
        if (StrUtil.isNotBlank(secret)) {
            jwtSecret = secret.trim();
        }
    }

    @PostConstruct
    public void init() {
        // Redis key 按 profile 隔离，避免各环境 token 互相覆盖
        String profile = StrUtil.isBlank(activeProfile) ? "local" : activeProfile.split(",")[0].trim();
        tokenPrefix = "keystone:token:" + profile + ":";
        log.info("Keystone JWT 内存缓存 key 前缀(环境隔离)：{}", tokenPrefix);
        if (StrUtil.isBlank(jwtSecret)) {
            throw new IllegalStateException("必须配置 JWT 签名密钥：请设置环境变量 JWT_SECRET（对应配置 app.jwt.secret）为强随机值；本地开发请确保加载 application-local.yml");
        }
    }

    /** 解析 token：优先请求头，其次同名 HttpOnly Cookie（不读 URL 参数，避免泄露） */
    public static String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String token = request.getHeader(TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (TOKEN_COOKIE.equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        return StrUtil.isBlank(token) ? null : token;
    }

    /** 签发管理端 token（有效期 30 天），并缓存到内存；缓存中已有且有效的 token 直接复用 */
    public String createManagementToken(String username) {
        String cached = tokenCache.get(tokenPrefix + username);
        if (StrUtil.isNotBlank(cached) && isTokenReusable(cached)) {
            return cached;
        }
        Date expires = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
        String token = JWT.create()
                .withAudience(username)
                .withIssuedAt(new Date())
                .withExpiresAt(expires)
                .sign(Algorithm.HMAC256(jwtSecret));
        tokenCache.put(tokenPrefix + username, token);
        return token;
    }

    private boolean isTokenReusable(String token) {
        try {
            parseAndVerifyToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Keystone 缓存 token 校验失败（签名不符或已过期），重新签发：{}", e.getMessage());
            return false;
        }
    }

    /** 校验 token 是否仍是该用户「当前有效」的登录 token（与签发缓存比对，登出后即失效） */
    public static boolean isTokenCurrent(String username, String token) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(token)) {
            return false;
        }
        return StrUtil.equals(tokenCache.get(tokenPrefix + username), token);
    }

    /** 解析并校验 token（签名 + 过期），失败抛业务异常（code=1001 未登录） */
    public static DecodedJWT parseAndVerifyToken(String token) {
        try {
            JWT.decode(token); // 先解码，确保格式合法
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            return verifier.verify(token);
        } catch (Exception e) {
            log.info("Keystone token 解析或校验失败：{}", e.getMessage());
            throw new BusinessException(1001, "token解析失败或已过期");
        }
    }

    /** 退出登录：删除缓存的 token */
    public void invalidateToken(String username) {
        if (StrUtil.isNotBlank(username)) {
            tokenCache.remove(tokenPrefix + username);
        }
    }

    /**
     * 生成与登录 token 绑定的 CSRF token（HMAC-SHA256，密钥为服务器端 JWT 密钥）。
     * <p>服务端无状态：即便攻击者能在子域写入同名 Cookie，也算不出与当前 token 匹配的值。</p>
     */
    public static String buildCsrfToken(String authToken) {
        if (StrUtil.isBlank(authToken)) {
            return null;
        }
        return hmac("keystone:csrf|" + authToken);
    }

    /** 构造 CSRF Cookie 的 Set-Cookie 值（非 HttpOnly：前端需读取其值回传请求头） */
    public static String buildCsrfCookie(String authToken, boolean secure) {
        return CSRF_COOKIE + "=" + buildCsrfToken(authToken)
                + "; Path=/; Max-Age=" + TOKEN_MAX_AGE_SECONDS
                + "; SameSite=Lax" + (secure ? "; Secure" : "");
    }

    /** CSRF 双提交校验：Cookie 值、请求头值都须等于当前登录 token 对应的签名值 */
    public static boolean isCsrfTokenValid(String cookieCsrf, String headerCsrf, String authToken) {
        if (StrUtil.isBlank(cookieCsrf) || StrUtil.isBlank(headerCsrf) || StrUtil.isBlank(authToken)) {
            return false;
        }
        String expected = hmac("keystone:csrf|" + authToken);
        return constantTimeEquals(expected, cookieCsrf) && constantTimeEquals(expected, headerCsrf);
    }

    private static String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Keystone CSRF token 生成失败", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}