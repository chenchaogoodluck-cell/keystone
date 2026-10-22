package com.keystone.admin.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.keystone.admin.annotation.EnvType;
import com.keystone.admin.constant.DataSourceNames;
import com.keystone.admin.constant.EnvConstants;
import org.springframework.stereotype.Service;

/**
 * local 环境「Demo 业务域」数据服务：查询动态注册的 {@code localDemo} 数据源（keystone_demo 库）
 * <p>
 * 组成：local（环境）+ Demo（业务域）= localDemo（见 DataSourceNames / DynamicDataSourceProperties）。
 * {@code @EnvType(LOCAL)} 声明本实现承载 local 环境数据，供门面按 env 路由。
 */
@Service
@DS(DataSourceNames.LOCAL_DEMO)
@EnvType(EnvConstants.LOCAL)
public class LocalDemoTagService extends AbstractDemoTagService {

}