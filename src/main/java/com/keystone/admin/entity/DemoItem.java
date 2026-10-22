package com.keystone.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 演示业务数据实体（对应表 sys_demo_item）
 */
@Data
@TableName("sys_demo_item")
public class DemoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 编码 */
    private String code;

    /** 名称 */
    private String name;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}