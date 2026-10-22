package com.keystone.admin.constant;

/**
 * 通用常量（数字 / 状态等）
 */
public final class CommonConstants {

    private CommonConstants() {
        throw new RuntimeException("Must not instantiate constant utility class");
    }

    public static final Integer INT_0 = 0;
    public static final Integer INT_1 = 1;
    public static final Integer INT_2 = 2;
    public static final Integer INT_3 = 3;
    public static final Integer INT_4 = 4;
    public static final Integer INT_5 = 5;

    public static final Long LONG_0 = 0L;
    public static final Long LONG_1 = 1L;

    /** 启用状态 */
    public static final Integer STATUS_ENABLE = 1;
    /** 停用状态 */
    public static final Integer STATUS_DISABLE = 0;
}