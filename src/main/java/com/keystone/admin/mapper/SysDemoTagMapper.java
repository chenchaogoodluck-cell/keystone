package com.keystone.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keystone.admin.entity.SysDemoTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 演示「第二业务域」数据 Mapper（对应第二数据源 localDemo）
 */
@Mapper
public interface SysDemoTagMapper extends BaseMapper<SysDemoTag> {

}