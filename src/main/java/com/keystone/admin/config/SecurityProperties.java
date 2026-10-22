package com.keystone.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keystone 安全配置（对应 application.yml 的 keystone.security.*）
 * <ul>
 *     <li>{@code admin-users}：内置管理员名单，名单内用户始终拥有全部页面访问权限；</li>
 *     <li>{@code users}：可登录账号列表（username=password，逗号分隔）。默认演示账号 admin/123456；
 *         生产请替换为对接用户表/AD/LDAP 的 {@code AuthService} 实现；</li>
 *     <li>{@code menu-auth-strict}：菜单页面权限是否严格模式，默认 false（开放）。</li>
 * </ul>
 */
@Data
@Component
@ConfigurationProperties(prefix = "keystone.security")
public class SecurityProperties {

    /** 内置管理员名单 */
    private List<String> adminUsers = new ArrayList<>(Collections.singletonList("admin"));

    /** 可登录账号：username=password，逗号分隔 */
    private String users = "admin=123456";

    /** 菜单页面权限严格模式：true 时非管理员仅可访问未标注注解的页面 */
    private boolean menuAuthStrict = false;

    /** 判断是否为内置管理员 */
    public boolean isAdminUser(String username) {
        return username != null && adminUsers != null && adminUsers.contains(username);
    }

    /** 解析 users 为 Map（username → password） */
    public Map<String, String> parseUsers() {
        Map<String, String> map = new LinkedHashMap<>();
        if (users == null) {
            return map;
        }
        for (String item : users.split(",")) {
            int idx = item.indexOf('=');
            if (idx > 0) {
                map.put(item.substring(0, idx).trim(), item.substring(idx + 1).trim());
            }
        }
        return map;
    }
}