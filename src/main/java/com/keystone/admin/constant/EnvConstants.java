package com.keystone.admin.constant;

/**
 * 环境类型常量
 * <p>对应前端「按环境分发」的参数取值（env / type），用于策略工厂
 * {@code EnvStrategy.envType()} 决定路由到哪个环境数据源服务。</p>
 */
public final class EnvConstants {

    private EnvConstants() {
        throw new RuntimeException("Must not instantiate constant utility class");
    }

    public static final int LOCAL = 1;
    public static final int DEV = 2;
    public static final int TEST = 3;
    public static final int PROD = 4;

    /** 环境值 → 展示名 */
    public static String label(Integer env) {
        if (env == null) {
            return "";
        }
        switch (env) {
            case LOCAL:
                return "local";
            case DEV:
                return "dev";
            case TEST:
                return "test";
            case PROD:
                return "prod";
            default:
                return "env" + env;
        }
    }
}