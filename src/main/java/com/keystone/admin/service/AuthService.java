package com.keystone.admin.service;

import cn.hutool.core.util.StrUtil;
import com.keystone.admin.config.SecurityProperties;
import com.keystone.admin.exception.BusinessException;
import com.keystone.admin.utils.JwtUtils;
import com.keystone.admin.vo.LoginReqVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务：登录鉴权 + 签发 token
 * <p>
 * 默认从 {@code keystone.security.users}（application.yml）读取账号。对接真实用户体系时，
 * 把 {@code validate}（校验用户名密码）替换为查询用户表 / AD / LDAP 即可，其余逻辑不变。
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private SecurityProperties securityProperties;

    @Resource
    private JwtUtils jwtUtils;

    /** 登录：校验账号密码，成功返回 token + username */
    public Map<String, String> login(LoginReqVo reqVo) {
        String username = reqVo.getUsername().trim();
        if (!validate(username, reqVo.getPassword())) {
            log.warn("Keystone 登录失败：用户名或密码错误，username={}", username);
            throw new BusinessException(1002, "用户名或密码错误");
        }
        Map<String, String> result = new HashMap<>();
        result.put("token", jwtUtils.createManagementToken(username));
        result.put("username", username);
        log.info("Keystone 登录成功：username={}", username);
        return result;
    }

    /** 校验用户名密码（扩展点：替换为 DB / AD / LDAP 查询） */
    private boolean validate(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return false;
        }
        Map<String, String> users = securityProperties.parseUsers();
        String pwd = users.get(username);
        return pwd != null && pwd.equals(password);
    }
}