package com.keystone.admin.controller;

import com.keystone.admin.config.CurrentUserHolder;
import com.keystone.admin.utils.JwtUtils;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 当前用户接口：退出登录 / 个人资料
 */
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Resource
    private JwtUtils jwtUtils;

    /** 退出登录：删除缓存的 token */
    @RequestMapping("/logout")
    public ApiResult<Void> logout() {
        String username = CurrentUserHolder.getName();
        jwtUtils.invalidateToken(username);
        log.info("Keystone 用户退出登录：username={}", username);
        return ApiResultUtils.buildResult();
    }

    /** 当前登录用户信息（个人资料页使用） */
    @RequestMapping("/getInfo")
    public ApiResult<Map<String, Object>> getInfo() {
        String username = CurrentUserHolder.getName();
        Map<String, Object> info = new HashMap<>(6);
        info.put("username", username);
        info.put("name", username);
        info.put("title", "Keystone 用户");
        info.put("email", "");
        info.put("phoneNumber", "");
        info.put("sex", "");
        info.put("hiredDate", "");
        info.put("workPlace", "暂无");
        return ApiResultUtils.buildResult(info);
    }
}