package com.keystone.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 菜单页面权限注解
 * <p>
 * 标注在 Controller 页面跳转方法上，声明该页面(菜单)的权限标识。
 * 校验逻辑见 {@link com.keystone.admin.config.MenuPermissionInterceptor}：
 * <ul>
 *     <li>内置管理员（keystone.security.admin-users，默认 admin）始终拥有全部页面访问权限；</li>
 *     <li>未标注注解的接口（登录页、主页、404/500、个人资料等）始终放行；</li>
 *     <li>{@code keystone.security.menu-auth-strict=false}（默认）时任意已登录用户可访问；
 *         为 {@code true} 时非管理员用户将被拦截到无权限页。</li>
 * </ul>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuPermission {

    /** 权限标识，如 demo:list */
    String value();

    /** 菜单名称（仅用于日志/提示），如"示例列表" */
    String name() default "";
}