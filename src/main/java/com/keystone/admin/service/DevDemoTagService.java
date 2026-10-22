package com.keystone.admin.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keystone.admin.annotation.EnvType;
import com.keystone.admin.constant.DataSourceNames;
import com.keystone.admin.constant.EnvConstants;
import org.springframework.stereotype.Service;

/**
 * dev 环境「Demo 业务域」数据服务：查询动态注册的 {@code devDemo} 数据源
 * <p>
 * 与 {@link LocalDemoTagService} 同构，仅 {@code @DS} 与 {@code @EnvType} 不同。
 * 前端按 env 调用门面时，策略工厂会路由到本实现。
 */
@Service
@DS(DataSourceNames.DEV_DEMO)
@EnvType(EnvConstants.DEV)
public class DevDemoTagService extends AbstractDemoTagService {

}