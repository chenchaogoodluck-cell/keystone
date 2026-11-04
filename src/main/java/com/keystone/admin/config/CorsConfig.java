package com.keystone.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置（白名单收敛）
 * <p>
 * 仅对 {@link CorsProperties#getAllowedOrigins()} 列出的来源放行与允许携带凭据；
 * 不再使用 {@code allowedOriginPattern("*")} + allowCredentials(true) 的开放组合
 * （该组合会使 HttpOnly Cookie 鉴权暴露给任意站点，配合 CSRF 有真实风险）。
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins != null) {
            for (String origin : origins) {
                if (origin != null && !origin.trim().isEmpty()) {
                    corsConfiguration.addAllowedOrigin(origin.trim());
                }
            }
        }
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        // 带凭据（Cookie/Authorization）跨域：仅在白名单来源允许，否则浏览器拒绝
        corsConfiguration.setAllowCredentials(true);
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }
}