package com.keystone.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keystone.admin.entity.DemoItem;
import com.keystone.admin.mapper.DemoItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 演示业务数据 Service
 */
@Service
public class DemoItemService extends ServiceImpl<DemoItemMapper, DemoItem> {

    /**
     * 条件分页查询（配合 PageHelper：调用方 startPage 后再调用本方法）
     */
    public List<DemoItem> pageQuery(String name, Integer status, String code) {
        LambdaQueryWrapper<DemoItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), DemoItem::getName, name)
                .like(StringUtils.hasText(code), DemoItem::getCode, code)
                .eq(status != null, DemoItem::getStatus, status)
                .orderByDesc(DemoItem::getId);
        return list(wrapper);
    }
}