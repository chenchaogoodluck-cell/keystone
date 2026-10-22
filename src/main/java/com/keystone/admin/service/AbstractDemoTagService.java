package com.keystone.admin.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keystone.admin.entity.SysDemoTag;
import com.keystone.admin.mapper.SysDemoTagMapper;
import com.keystone.admin.service.strategy.DemoTagEnvService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;

/**
 * 演示标签「环境策略」服务抽象基类
 * <p>
 * 提供各环境共有的查询逻辑；具体数据源由子类上的 {@code @DS} 注解决定
 * （如 {@code LocalDemoTagService} 标注 {@code @DS("localDemo")} → 查询 local 环境 Demo 域库）。
 * 新增环境只需复制一个子类并标注 {@code @DS} + {@code @EnvType}，无需改动基类查询逻辑。
 * 环境类型由 {@code DemoTagEnvService#envType()} 供门面 {@code DemoTagStrategyService} 按 env 路由。
 */
@Slf4j
public abstract class AbstractDemoTagService extends ServiceImpl<SysDemoTagMapper, SysDemoTag> implements DemoTagEnvService {

    /** 当前子类标注的数据源名（读取子类 @DS 注解） */
    public String dsName() {
        DS ds = AnnotationUtils.findAnnotation(getClass(), DS.class);
        return ds == null ? "" : ds.value();
    }

    /** 查询全部标签（在当前子类指定的数据源执行） */
    @Override
    public List<SysDemoTag> listTags() {
        LambdaQueryWrapper<SysDemoTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysDemoTag::getId);
        List<SysDemoTag> list = list(wrapper);
        log.info("Keystone 环境策略演示：数据源 [{}] 查询 sys_demo_tag 返回 {} 条", dsName(), list.size());
        return list;
    }
}