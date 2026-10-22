package com.keystone.admin.service.strategy;

import com.keystone.admin.annotation.EnvType;

import java.lang.annotation.Annotation;

/**
 * 环境策略标记接口
 * <p>
 * 各环境服务实现类（local / dev / test / prod）通过 {@link EnvType} 注解声明对应环境类型，
 * 门面 Service 注入 {@code List<T>} 后按 {@link #envType()} 构建 {@code Map<Integer, T>}，
 * 用一行查找替代大量重复的 switch-case 分发（见 {@code DemoTagStrategyService}）。
 */
public interface EnvStrategy {

    /**
     * 返回当前实现对应的环境类型（读取类上的 {@link EnvType} 注解）
     *
     * @return 环境类型值，见 {@link com.keystone.admin.constant.EnvConstants}
     */
    default int envType() {
        Annotation annotation = getClass().getAnnotation(EnvType.class);
        if (annotation == null) {
            throw new IllegalStateException(getClass().getName() + " 缺少 @EnvType 注解，无法注册环境策略");
        }
        return ((EnvType) annotation).value();
    }
}