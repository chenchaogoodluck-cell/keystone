package com.keystone.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

/**
 * Web MVC 配置：注册登录拦截器、菜单权限拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    public WebMvcConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器：仅拦截 /api/** 数据接口，页面模板本身不鉴权（页面无敏感数据）
        registry.addInterceptor(new MyHandlerInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/auth/login");

        // 菜单页面权限拦截器：注解驱动（@MenuPermission），静态资源自动放行
        registry.addInterceptor(new MenuPermissionInterceptor(securityProperties))
                .addPathPatterns("/**");
    }

    /** 自定义视图解析器（预留，配合 freemarker 使用） */
    @Bean
    public ViewResolver keystoneViewResolver() {
        return new ViewResolver() {
            @Override
            public View resolveViewName(String viewName, Locale locale) {
                return null;
            }
        };
    }
}