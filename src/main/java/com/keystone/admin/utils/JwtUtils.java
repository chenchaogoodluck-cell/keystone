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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 工具：签发 / 校验登录 token
 * <ul>
 *     <li>前端将 token 放请求头 {@link #TOKEN_HEADER}；登录后后端同时写入同名 HttpOnly Cookie，
 *         以兼容 iframe 弹层 / 页面跳转等无法携带请求头的场景；</li>
 *     <li>签名密钥来自 {@code app.jwt.secret}（环境变量 JWT_SECRET），未配置则启动失败（fail-fast）；</li>
 *     <li>已签发 token 缓存在内存（按 profile 隔离），支持登录态管理（退出时删除）。
 *         当前为无 Redis 的内存实现；如需 Redis 分发/失效，替换为调用 RedisUtil 即可。</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtUtils {

    /** 内存 token 缓存：key=prefix+username，value=token（进程内有效，重启即失效） */
    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /** 请求头 / Cookie 中携带 token 的字段名（与 static/config.js setter.request.tokenName 保持一致） */
    public static final String TOKEN_HEADER = "x-keystone-auth-token";
    public static final String TOKEN_COOKIE = "x-keystone-auth-token";

    /** 服务器端 JWT 签名密钥（从配置注入，未配置则启动失败） */
    private static String jwtSecret;

    private String tokenPrefix;

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

    /** 获取签发对象（用户名） */
    public static String getAudience(String token) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(1001, "未登录或登录已过期");
        }
        return JWT.decode(token).getAudience().get(0);
    }

    /** 退出登录：删除缓存的 token */
    public void invalidateToken(String username) {
        if (StrUtil.isNotBlank(username)) {
            tokenCache.remove(tokenPrefix + username);
        }
    }
}