package com.keystone.admin.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.keystone.admin.annotation.MenuPermission;
import com.keystone.admin.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 菜单页面权限拦截器（注解驱动）
 * <ul>
 *     <li>仅校验标注了 {@link MenuPermission} 的 Controller 方法，静态资源自动放行；</li>
 *     <li>内置管理员（keystone.security.admin-users，默认 admin）始终拥有全部页面权限；</li>
 *     <li>{@code menu-auth-strict=false}（默认）：任意已登录用户均可访问已标注页面，注解仅用于前端菜单渲染；</li>
 *     <li>{@code menu-auth-strict=true}：非管理员访问已标注页面 → forward 到无权限页；</li>
 *     <li>未登录 / token 无效 → 重定向登录页。</li>
 * </ul>
 */
@Slf4j
public class MenuPermissionInterceptor implements HandlerInterceptor {

    private final SecurityProperties securityProperties;

    public MenuPermissionInterceptor(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 仅校验 Controller 方法；静态资源等 handler 直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        MenuPermission menuPermission = handlerMethod.getMethodAnnotation(MenuPermission.class);

        // 2. 未标注权限注解的接口始终放行
        if (menuPermission == null) {
            return true;
        }

        // 3. 获取当前登录用户：优先请求头，其次 HttpOnly Cookie
        String token = JwtUtils.resolveToken(request);
        if (token == null || token.trim().isEmpty()) {
            log.info("Keystone 菜单权限校验：未携带 token，重定向登录页，uri={}", request.getRequestURI());
            response.sendRedirect("/login");
            return false;
        }
        String username;
        try {
            username = JwtUtils.parseAndVerifyToken(token).getAudience().get(0);
        } catch (Exception e) {
            log.warn("Keystone 菜单权限校验：token 无效或已过期，重定向登录页，uri={}", request.getRequestURI());
            response.sendRedirect("/login");
            return false;
        }

        // 4. 内置管理员：拥有全部页面权限
        if (securityProperties.isAdminUser(username)) {
            return true;
        }

        // 5. 非严格模式（默认）：任意已登录用户可访问，注解仅用于前端菜单渲染
        if (!securityProperties.isMenuAuthStrict()) {
            return true;
        }

        // 6. 严格模式：非管理员无权访问
        log.warn("Keystone 菜单权限校验（严格模式）：用户 [{}] 无权访问页面 [{}]({})，uri={}",
                username, menuPermission.value(), menuPermission.name(), request.getRequestURI());
        request.setAttribute("permission", menuPermission.value());
        request.setAttribute("permissionName", menuPermission.name());
        request.getRequestDispatcher("/permission/denied").forward(request, response);
        return false;
    }
}