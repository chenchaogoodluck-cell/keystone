package com.keystone.admin.controller;

import com.keystone.admin.annotation.MenuPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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

    /** 登录成功后进入主框架页 */
    @RequestMapping(value = "/index/{username}")
    public String main(@PathVariable String username, Model model) {
        model.addAttribute("username", username);
        return "index";
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