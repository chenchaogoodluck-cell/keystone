package com.keystone.admin.service;

import com.keystone.admin.config.SecurityProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 菜单权限服务：为前端提供当前用户可访问页面的权限标识集合
 * <p>
 * 仅用于前端菜单/快捷方式的渲染（决定哪些菜单可见）；真正的访问安全由
 * {@code MenuPermissionInterceptor}（@MenuPermission 注解 + 拦截器）兜底。
 * <ul>
 *     <li>内置管理员：allPermitted = true（前端全显菜单）；</li>
 *     <li>非严格模式（默认）：allPermitted = true；</li>
 *     <li>严格模式：按用户配置的权限标识过滤（扩展点：可改为查库/Redis 返回该用户权限列表）。</li>
 * </ul>
 */
@Service
public class MenuPermissionService {

    @Resource
    private SecurityProperties securityProperties;

    /** 查询当前用户菜单权限，返回给 /api/v1/menuPermission/current */
    public Map<String, Object> currentPermissions(String username) {
        Map<String, Object> result = new HashMap<>(4);
        boolean admin = securityProperties.isAdminUser(username);
        boolean strict = securityProperties.isMenuAuthStrict();
        if (admin || !strict) {
            result.put("allPermitted", true);
            result.put("admin", admin);
            result.put("strict", strict);
            result.put("permissions", java.util.Collections.emptyList());
        } else {
            // 严格模式：此处为扩展点，可根据用户从 DB/Redis 读取其权限集合
            result.put("allPermitted", false);
            result.put("admin", false);
            result.put("strict", true);
            result.put("permissions", java.util.Collections.emptyList());
        }
        return result;
    }
}