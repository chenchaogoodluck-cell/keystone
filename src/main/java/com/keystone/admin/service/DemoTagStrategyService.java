package com.keystone.admin.service;

import com.keystone.admin.service.strategy.DemoTagEnvService;
import com.keystone.admin.service.strategy.EnvStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 演示标签「环境策略选service」门面
 * <p>
 * 借助 Spring 注入全部 {@link DemoTagEnvService} 实现（各环境子类），
 * 按 {@code EnvStrategy.envType()} 构建 {@code Map<Integer, DemoTagEnvService>}，
 * 前端传 env 时一行查找即命中对应环境数据源服务 —— 替代大量重复 switch-case 分发。
 * 新增环境只需新增一个带 {@code @DS} + {@code @EnvType} 的实现类，本门面零改动。
 */
@Slf4j
@Service
public class DemoTagStrategyService {

    /** 环境类型 -> 环境服务 */
    private final Map<Integer, DemoTagEnvService> envServices;

    public DemoTagStrategyService(List<DemoTagEnvService> envServices) {
        this.envServices = envServices.stream()
                .collect(Collectors.toMap(EnvStrategy::envType, Function.identity()));
        log.info("Keystone 环境策略门面已注册 {} 个环境服务：{}", this.envServices.size(), this.envServices.keySet());
    }

    /** 按环境返回对应数据源服务；未注册的环境返回 null */
    public DemoTagEnvService getEnvService(int env) {
        return envServices.get(env);
    }

    /** 当前已注册的所有环境类型 → 展示名 */
    public Map<Integer, String> availableEnvs() {
        return envServices.keySet().stream()
                .collect(Collectors.toMap(Function.identity(),
                        env -> com.keystone.admin.constant.EnvConstants.label(env)));
    }
}