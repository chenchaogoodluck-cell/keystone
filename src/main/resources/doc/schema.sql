-- =====================================================================
-- Keystone 脚手架演示表结构
-- 说明：
--  · 库 keystone           —— 主数据源（动态数据源 primary=master），承载 sys_demo_item
--  · 库 keystone_demo      —— 第二数据源（由 MultiDataSourceRegister 动态注册为 localDemo），
--                             承载 sys_demo_tag，演示 @DS 注解跨数据源查询
-- 业务模块请把本文件按需扩展/替换。执行前请先建库：
--   CREATE DATABASE keystone      DEFAULT CHARSET utf8mb4;
--   CREATE DATABASE keystone_demo DEFAULT CHARSET utf8mb4;
-- =====================================================================

-- =====================================================================
-- 主库 keystone
-- =====================================================================

-- 演示模块：业务数据（通用增删改查）
CREATE TABLE IF NOT EXISTS `sys_demo_item` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `code`        VARCHAR(64)  NOT NULL COMMENT '编码',
    `name`        VARCHAR(128) NOT NULL COMMENT '名称',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '演示业务数据表';

INSERT INTO `sys_demo_item` (`code`, `name`, `status`, `remark`) VALUES
('P001', '示例数据A', 1, '演示记录 1'),
('P002', '示例数据B', 1, '演示记录 2'),
('P003', '示例数据C', 0, '演示记录 3');

-- =====================================================================
-- 第二数据源库 keystone_demo（动态注册为 localDemo，配合 @DS 注解演示跨数据源）
-- =====================================================================

CREATE TABLE IF NOT EXISTS `keystone_demo`.`sys_demo_tag` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_name`    VARCHAR(64)  NOT NULL COMMENT '标签名',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '演示第二数据源：标签表';

INSERT INTO `keystone_demo`.`sys_demo_tag` (`tag_name`, `remark`) VALUES
('标签A', '来自 keystone_demo 库'),
('标签B', '动态数据源 localDemo'),
('标签C', '@DS 注解切换查询');