package com.keystone.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.keystone.admin.annotation.MenuPermission;
import com.keystone.admin.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 页面路由：统一返回 FreeMarker 模板视图
 */
@Controller
@RequestMapping("/")
@Slf4j
public class SystemController {

    /** 根路径 → 登录页 */
    @RequestMapping(value = "/")
    public String index() {
        return "user/login";
    }

    /** 登录页 */
    @RequestMapping(value = "/login")
    public String login() {
        return "user/login";
    }

    /** 登录成功后进入主框架页：用户名从登录 token 读取（不放入 URL，避免泄露/被篡改） */
    @RequestMapping(value = "/index")
    public String main(HttpServletRequest request, Model model) {
        String username = resolveLoginUsername(request);
        if (StrUtil.isBlank(username)) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "index";
    }

    private String resolveLoginUsername(HttpServletRequest request) {
        String token = JwtUtils.resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            String username = JwtUtils.parseAndVerifyToken(token).getAudience().get(0);
            // 与鉴权拦截器保持一致：登出后 token 已移出签发缓存，不能再进入框架页（否则旧 Cookie 会渲染出进不了接口的空壳页）
            if (StrUtil.isBlank(username) || !JwtUtils.isTokenCurrent(username, token)) {
                return null;
            }
            return username;
        } catch (Exception e) {
            log.warn("Keystone 访问主框架页时 token 无效或已过期：{}", e.getMessage());
            return null;
        }
    }

    /** 主页（首页 dashboard，iframe 内加载） */
    @RequestMapping(value = "/home/main")
    public String console() {
        return "home/main";
    }

    /** 404 */
    @RequestMapping(value = "/404")
    public String error404() {
        return "error";
    }

    /** 500 */
    @RequestMapping(value = "/500")
    public String error500() {
        return "home/error";
    }

    /** 无权限页（菜单权限拦截器 forward 到此） */
    @RequestMapping(value = "/permission/denied")
    public String permissionDenied(HttpServletRequest request, Model model) {
        model.addAttribute("permission", request.getAttribute("permission"));
        model.addAttribute("permissionName", request.getAttribute("permissionName"));
        return "permission/denied";
    }

    /** 个人资料 */
    @RequestMapping(value = "/user/info")
    public String userInfo() {
        return "user/info";
    }

    // ==================== 示例模块（演示新增模块页面路由） ====================

    @RequestMapping(value = "/demo/list")
    @MenuPermission(value = "demo:list", name = "示例列表")
    public String demoList() {
        return "demo/list";
    }

    @RequestMapping(value = "/demo/add")
    @MenuPermission(value = "demo:add", name = "示例新增")
    public String demoAdd() {
        return "demo/add";
    }

    @RequestMapping(value = "/demo/datasource")
    @MenuPermission(value = "demo:datasource", name = "多数据源演示")
    public String demoDatasource() {
        return "demo/datasource";
    }
}