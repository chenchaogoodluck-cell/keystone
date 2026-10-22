package com.keystone.admin.annotation;

import com.keystone.admin.constant.EnvConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 环境类型注解，标注在「按环境分发的数据源服务」实现类上。
 * <p>value 取值见 {@link EnvConstants}（LOCAL / DEV / TEST / PROD），
 * 由 {@code EnvStrategy.envType()} 读取，供策略工厂按前端传的 env 选择对应 service。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvType {

    /** 环境类型值，见 {@link EnvConstants} */
    int value();
}