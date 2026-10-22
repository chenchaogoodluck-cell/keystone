package com.keystone.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动态数据源配置（对应 application.yml 的 keystone.datasource.*）
 * <p>
 * 数据源 = 环境连接参数 × 业务域库名，启动时由 {@link MultiDataSourceRegister} 按笛卡尔积
 * 自动生成并注册全部数据源，数据源名称 = 环境名 + 业务域后缀（如 local + Demo = localDemo）。
 * 新增环境或业务域只需在此增加配置，无需改代码。
 */
@Data
@ConfigurationProperties(prefix = "keystone.datasource")
public class DynamicDataSourceProperties {

    /** 驱动类名，为空时由连接池按 url 自动识别 */
    private String driverClassName;

    /** url 公共参数后缀（不含 ?） */
    private String urlSuffix;

    /** 环境连接参数：环境名（local/dev/test/...） → 连接信息 */
    private Map<String, Env> environments = new LinkedHashMap<>();

    /** 业务域库名：业务域后缀（Demo/Log/...） → 数据库名 */
    private Map<String, String> databases = new LinkedHashMap<>();

    /**
     * 环境连接信息
     */
    @Data
    public static class Env {
        private String host;
        private Integer port = 3306;
        private String username;
        private String password;
    }
}