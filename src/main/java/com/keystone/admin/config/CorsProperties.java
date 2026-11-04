package com.keystone.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 跨域白名单配置（对应 application.yml 的 keystone.cors.*）
 * <p>
 * 不再使用 {@code allowedOriginPattern("*")} + allowCredentials 这种可带凭据的开放组合，
 * 改为显式白名单：仅列出的来源允许携带凭据跨域访问。
 */
@Data
@Component
@ConfigurationProperties(prefix = "keystone.cors")
public class CorsProperties {

    /** 允许跨域的来源（Origin），多个用逗号分隔，可通过环境变量 KEYSTONE_CORS_ORIGINS 覆盖 */
    private List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:9110",
            "http://127.0.0.1:9110"));
}