package com.keystone.admin.controller;

import com.keystone.admin.config.CurrentUserHolder;
import com.keystone.admin.service.MenuPermissionService;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 菜单权限接口：返回当前用户可访问菜单的权限标识（供前端渲染菜单）
 */
@RestController
@RequestMapping("/api/v1/menuPermission")
public class MenuPermissionController {

    @Resource
    private MenuPermissionService menuPermissionService;

    @RequestMapping("/current")
    public ApiResult<Map<String, Object>> current() {
        String username = CurrentUserHolder.getName();
        return ApiResultUtils.buildResult(menuPermissionService.currentPermissions(username));
    }
}