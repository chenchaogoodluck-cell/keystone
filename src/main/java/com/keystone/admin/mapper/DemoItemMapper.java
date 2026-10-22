package com.keystone.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keystone.admin.entity.DemoItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 演示业务数据 Mapper
 * <p>继承 {@link BaseMapper} 即可获得增删改查；复杂 SQL 可在此声明方法并配对应
 * {@code src/main/resources/mapper/DemoItemMapper.xml}。</p>
 */
@Mapper
public interface DemoItemMapper extends BaseMapper<DemoItem> {

}