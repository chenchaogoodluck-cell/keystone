package com.keystone.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.keystone.admin.entity.DemoItem;
import com.keystone.admin.mapper.DemoItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
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
        // 转义 LIKE 通配符，避免用户输入 %/_ 被当作通配符扩大匹配（依赖 MySQL 默认转义字符 \）
        wrapper.like(StringUtils.hasText(name), DemoItem::getName, escapeLike(name))
                .like(StringUtils.hasText(code), DemoItem::getCode, escapeLike(code))
                .eq(status != null, DemoItem::getStatus, status)
                .orderByDesc(DemoItem::getId);
        return list(wrapper);
    }

    private String escapeLike(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * 批量删除：整批置于同一事务，任一条失败则全部回滚，避免部分删除
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            removeByIds(ids);
        }
    }
}