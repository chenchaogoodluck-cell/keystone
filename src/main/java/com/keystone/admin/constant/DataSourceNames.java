package com.keystone.admin.constant;

/**
 * 数据源名称常量
 * <p>
 * 命名规则：环境前缀（local/dev/test/test02/prod）+ 业务域后缀（Demo/Log...），
 * 由 {@link com.keystone.admin.config.MultiDataSourceRegister} 启动时按「环境 × 业务域」动态注册生成。
 * 例如：local 环境 + Demo 业务域 = {@code localDemo}。
 */
public final class DataSourceNames {

    private DataSourceNames() {
        throw new RuntimeException("Must not instantiate constant utility class");
    }

    /** 默认主数据源（keystone 库） */
    public static final String MASTER = "master";

    // ===== Demo 业务域（库 keystone_demo）=====
    public static final String LOCAL_DEMO = "localDemo";
    public static final String DEV_DEMO = "devDemo";
    public static final String TEST_DEMO = "testDemo";
    public static final String PROD_DEMO = "prodDemo";
}