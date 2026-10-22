package com.keystone.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 演示「第二业务域」实体（对应第二数据源库 {@code keystone_demo}.sys_demo_tag）
 * <p>该数据存放在由 {@code MultiDataSourceRegister} 动态注册的 localDemo 数据源中，
 * 用于演示 {@code @DS} 注解跨数据源查询。</p>
 */
@Data
@TableName("sys_demo_tag")
public class SysDemoTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 标签名 */
    private String tagName;

    /** 备注 */
    private String remark;

    @TableField("create_time")
    private Date createTime;
}