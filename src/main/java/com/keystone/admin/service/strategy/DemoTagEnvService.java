package com.keystone.admin.service.strategy;

import com.keystone.admin.entity.SysDemoTag;

import java.util.List;

/**
 * 演示标签「环境策略」接口
 * <p>
 * 由 local / dev 各环境服务实现；每个实现类通过 {@code @DS} 指定自己的数据源、
 * 通过 {@code @EnvType} 声明环境类型。门面 {@code DemoTagStrategyService} 据此按 env 自动路由。
 */
public interface DemoTagEnvService extends EnvStrategy {

    /** 当前实现标注的数据源名（读取类上 @DS 注解） */
    String dsName();

    /**
     * 查询当前环境数据源的标签列表
     *
     * @return 标签列表
     */
    List<SysDemoTag> listTags();
}