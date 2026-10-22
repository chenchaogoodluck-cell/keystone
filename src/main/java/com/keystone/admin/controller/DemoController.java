package com.keystone.admin.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.keystone.admin.entity.DemoItem;
import com.keystone.admin.entity.SysDemoTag;
import com.keystone.admin.exception.BusinessException;
import com.keystone.admin.service.DemoItemService;
import com.keystone.admin.service.DemoTagStrategyService;
import com.keystone.admin.service.LocalDemoTagService;
import com.keystone.admin.service.strategy.DemoTagEnvService;
import com.keystone.admin.web.ApiPageResult;
import com.keystone.admin.web.ApiResult;
import com.keystone.admin.web.ApiResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示业务数据接口
 * <p>这是新增业务模块的完整范式：Controller + Service + Mapper + Entity + layui 页面。
 * 新增业务时，复制本模块并按业务重命名即可。</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

    @Resource
    private DemoItemService demoItemService;

    /** 第二数据源（localDemo → keystone_demo 库）演示服务 */
    @Resource
    private LocalDemoTagService localDemoTagService;

    /** 环境策略门面：按前端传的 env 自动选择对应环境的数据源服务 */
    @Resource
    private DemoTagStrategyService demoTagStrategyService;

    /**
     * 分页查询（layui table 约定：page + limit + 查询条件），返回 code/count/data
     */
    @RequestMapping("/page")
    public ApiPageResult<DemoItem> page(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "15") int limit,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Integer status) {
        PageHelper.startPage(page, limit);
        List<DemoItem> list = demoItemService.pageQuery(name, status, null);
        return ApiResultUtils.buildPageResult((Page<?>) list, list);
    }

    /** 新增 */
    @RequestMapping("/add")
    public ApiResult<Void> add(@RequestBody DemoItem item) {
        demoItemService.save(item);
        return ApiResultUtils.buildResult();
    }

    /** 编辑 */
    @RequestMapping("/update")
    public ApiResult<Void> update(@RequestBody DemoItem item) {
        if (item.getId() == null) {
            throw new BusinessException(4000, "id不能为空");
        }
        demoItemService.updateById(item);
        return ApiResultUtils.buildResult();
    }

    /** 删除（支持 id 或 id,id,id） */
    @RequestMapping("/delete")
    public ApiResult<Void> delete(@RequestParam String ids) {
        for (String id : ids.split(",")) {
            demoItemService.removeById(Long.valueOf(id.trim()));
        }
        return ApiResultUtils.buildResult();
    }

    /**
     * 多数据源切换演示：一次请求内分别查询「主数据源」（master → keystone.sys_demo_item）
     * 与「动态注册的第二数据源」（localDemo → keystone_demo.sys_demo_tag）。
     */
    @RequestMapping("/datasource")
    public ApiResult<Map<String, Object>> datasourceDemo() {
        Map<String, Object> result = new HashMap<>(8);
        // 1. 主数据源（master，默认源）：DemoItemService 未标注 @DS，走 primary=master
        result.put("masterItems", demoItemService.pageQuery(null, null, null));
        // 2. 第二数据源（localDemo，@DS 注解切源）：LocalDemoTagService 标注 @DS(DataSourceNames.LOCAL_DEMO)
        List<SysDemoTag> tags = localDemoTagService.listTags();
        result.put("demoDsName", localDemoTagService.dsName());
        result.put("demoTags", tags);
        // 3. 再次切回主数据源
        result.put("masterAfter", demoItemService.pageQuery(null, null, null).size());
        return ApiResultUtils.buildResult(result);
    }

    /**
     * 环境策略 demo：前端传 env（1=local, 2=dev），策略工厂自动路由到对应环境数据源服务，
     * 无需在 Controller 里写任何 if-else / switch。
     */
    @RequestMapping("/env")
    public ApiResult<Map<String, Object>> byEnv(@RequestParam(defaultValue = "1") int env) {
        Map<String, Object> result = new HashMap<>(6);
        result.put("availableEnvs", demoTagStrategyService.availableEnvs()); // 供前端渲染下拉
        DemoTagEnvService service = demoTagStrategyService.getEnvService(env);
        if (service == null) {
            return ApiResultUtils.buildResult(4000, "未注册环境：" + env + "（可选：" + demoTagStrategyService.availableEnvs() + "）");
        }
        result.put("selectedEnv", env);
        result.put("selectedDs", service.dsName());
        result.put("tags", service.listTags());
        return ApiResultUtils.buildResult(result);
    }
}