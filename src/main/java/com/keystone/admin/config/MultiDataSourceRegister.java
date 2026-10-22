package com.keystone.admin.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.keystone.admin.constant.DataSourceNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 多数据源动态注册器
 * <p>
 * 启动时按「环境 × 业务域」读取 {@link DynamicDataSourceProperties}，动态构建并注册全部数据源
 * 到 {@link DynamicRoutingDataSource}，供各 service 上的 {@code @DS} 注解引用，
 * 实现新增环境 / 业务域免改代码即动态加载。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class MultiDataSourceRegister {

    @Resource
    private DynamicDataSourceProperties properties;

    @Resource(name = "dataSource")
    private DataSource dynamicDataSource;

    @Resource
    private DefaultDataSourceCreator dataSourceCreator;

    @PostConstruct
    public void registerDataSources() {
        if (properties.getDatabases() == null || properties.getEnvironments() == null
                || properties.getDatabases().isEmpty() || properties.getEnvironments().isEmpty()) {
            log.info("Keystone 动态数据源配置为空，跳过动态注册（将仅使用主数据源 {}）",
                    DataSourceNames.MASTER);
            return;
        }
        DynamicRoutingDataSource routingDataSource = (DynamicRoutingDataSource) dynamicDataSource;
        int count = 0;
        for (String bizSuffix : properties.getDatabases().keySet()) {
            String database = properties.getDatabases().get(bizSuffix);
            for (String env : properties.getEnvironments().keySet()) {
                DynamicDataSourceProperties.Env envCfg = properties.getEnvironments().get(env);
                String dsName = env + bizSuffix;
                DataSource dataSource = buildDataSource(dsName, envCfg, database);
                routingDataSource.addDataSource(dsName, dataSource);
                count++;
                log.info("Keystone 动态注册数据源：{} → {}/{}@{}:{}",
                        dsName, database, envCfg.getUsername(), envCfg.getHost(), envCfg.getPort());
            }
        }
        log.info("Keystone 动态数据源注册完成，共 {} 个", count);
    }

    private DataSource buildDataSource(String dsName, DynamicDataSourceProperties.Env envCfg, String database) {
        String url = "jdbc:mysql://" + envCfg.getHost() + ":" + envCfg.getPort() + "/" + database
                + (StrUtil.isBlank(properties.getUrlSuffix()) ? "" : "?" + properties.getUrlSuffix());
        DataSourceProperty property = new DataSourceProperty();
        property.setPoolName(dsName);
        property.setType(DruidDataSource.class);
        property.setUrl(url);
        property.setUsername(envCfg.getUsername());
        property.setPassword(envCfg.getPassword());
        if (StrUtil.isNotBlank(properties.getDriverClassName())) {
            property.setDriverClassName(properties.getDriverClassName());
        }
        return dataSourceCreator.createDataSource(property);
    }
}