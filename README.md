# Keystone · 通用单体管理后台脚手架

> 基石（拱心石）——从一个可运行的「登录 + 页面框架 + 拦截器 + 统一返回」开始，快速构建任意新的单体项目。
> 从既有 Spring Boot 单体项目抽取所得，去除了全部业务代码，只保留通用框架能力。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| 语言 / 框架 | Java 8 · Spring Boot 2.7.18 |
| 页面模板 | FreeMarker（`.html` 后缀） |
| 前端 UI | layuiAdmin（layui 2.13.6，iframe 版） |
| 持久层 | MyBatis + MyBatis-Plus 3.3.2 + PageHelper |
| 数据库 | MySQL 8.0.33（Druid 1.2.24 连接池 + 监控） + dynamic-datasource 3.6.1 动态多数据源 |
| 缓存 | Spring Data Redis（Lettuce 连接池） |
| 鉴权 | JWT（java-jwt 3.8.3，请求头 + HttpOnly Cookie） |
| 工具 | Hutool 5.7.15 · fastjson 1.2.83 · Lombok |

## 已内置的框架能力

- **登录**：JWT 签发 / 校验，token 写入请求头 `x-keystone-auth-token` 与 HttpOnly Cookie（兼容 iframe 弹层页面跳转）；签发后按「环境 + 用户」缓存于内存，登出即失效。
- **登录拦截器**：`MyHandlerInterceptor` 拦截 `.addPathPatterns("/api/**")` 所有数据接口，未登录 / token 失效统一返回 `code=1001`（前端据此跳登录页）。
- **菜单页面权限**：`@MenuPermission("xxx:list")` 注解 + `MenuPermissionInterceptor`。内置管理员（`keystone.security.admin-users`，默认 `admin`）拥有全部页面权限；默认非严格模式（`menu-auth-strict=false`），任意已登录用户可访问页面；可切换严格模式（页面进无权限页、`/api` 返回 `4010`）。
- **统一返回**：`ApiResult` / `ApiPageResult`（layui table 约定 `code/count/data`）+ `GlobalExceptionHandler` 统一异常。
- **当前用户**：`CurrentUserHolder`（ThreadLocal，`afterCompletion` 请求结束自动清理，防线程池复用串号）。
- **Redis 工具**：`RedisUtil` 常用操作封装（String/Hash/List/Set/ZSet/自增等）。
- **动态多数据源**：`MultiDataSourceRegister` 启动时按「环境 × 业务域」笛卡尔积动态注册到 `DynamicRoutingDataSource`，service 用 `@DS` 注解切换；新增环境/业务域无需改代码。demo 见下。
- **跨域**：`CorsFilter` 白名单收敛（仅列出的来源允许携带凭据），杜绝 `"*" + credentials` 组合。
- **CSRF 收敛**：写操作（登录 / 新增 / 编辑 / 删除 / 登出）显式 `POST`，配合 Cookie `SameSite=Lax` 与请求头鉴权。
- **页面框架**：layuiAdmin 主布局（顶部用户菜单 / 侧边栏 / 多标签页 / 主题切换 / 菜单权限过滤）+ 登录页 / 首页(快捷方式) / 404 / 500 / 403。
- **示例模块**：`sys_demo_item` 增删改查 + `多数据源演示`页 + `环境策略演示`，作为「新增业务模块」「跨数据源查询」「按 env 路由」的完整范式。

## 快速开始

前置：本地 JDK 8、MySQL、Redis。

1. **建库建表**：先建 `keystone` 与 `keystone_demo` 两个库，再执行 `src/main/resources/doc/schema.sql`（建主库演示表 + 第二库演示表）。
   ```sql
   CREATE DATABASE keystone       DEFAULT CHARSET utf8mb4;
   CREATE DATABASE keystone_demo  DEFAULT CHARSET utf8mb4;
   ```
2. **配置连接**：编辑 `config/application-local.yml` 中的主数据源（`spring.datasource.dynamic.datasource.master`）、动态数据源（`keystone.datasource`）与 Redis（默认指向 `127.0.0.1`）。本地默认口令可直接使用。
3. **JWT 密钥**：本地 profile 已内置开发密钥；非本地环境通过环境变量 `JWT_SECRET` 注入强随机值（未配置则启动 fail-fast）。
4. **启动**：运行 `KeystoneApplication`（默认 profile=`local`，端口 `9110`）。IDEA 内置 Maven + JDK8 导入即可，无全局 `mvn` 也能跑。
5. **访问**：<http://localhost:9110> ，默认账号 `admin / 123456`。
6. **数据源监控**：<http://127.0.0.1:9110/druid/index.html> 。默认仅允许本机回环访问，账号密码由 `KEYSTONE_DRUID_USER` / `KEYSTONE_DRUID_PWD` 配置（内置值仅供本地演示）；部署到非本机环境前必须修改，或用 `KEYSTONE_DRUID_ENABLED=false` 直接关闭。

打包运行：

```bash
mvn clean package            # 默认 local（resource 过滤仅作用于配置文件）
java -jar target/keystone-1.0.0-SNAPSHOT.jar
```

## 环境与配置

### 环境变量一览

敏感配置一律支持环境变量注入，`application-local.yml` / `application-dev.yml` 仅给出本地缺省值。

| 环境变量 | 对应配置 | 说明 |
| --- | --- | --- |
| `PROFILE` | `spring.profiles.active` | 运行期覆盖 Maven 打进包里的默认 profile |
| `JWT_SECRET` | `app.jwt.secret` | JWT 签名密钥，非本地环境必填 |
| `COOKIE_SECURE` | `app.jwt.cookie-secure` | 生产 HTTPS 设 `true`；本地/内网 http 保持 `false` |
| `KEYSTONE_USERS` | `keystone.security.users` | 可登录账号 `username=password`，多个逗号分隔 |
| `KEYSTONE_MENU_STRICT` | `keystone.security.menu-auth-strict` | 菜单页面权限是否严格模式 |
| `KEYSTONE_CORS_ORIGINS` | `keystone.cors.allowed-origins` | 允许携带凭据跨域的来源白名单 |
| `KEYSTONE_DB_URL` / `KEYSTONE_DB_USER` / `KEYSTONE_DB_PWD` | 主数据源 `master` | keystone 库连接 |
| `KEYSTONE_DEMO_DB_HOST` / `_PORT` / `_USER` / `_PWD` | `keystone.datasource.environments.*` | 动态「Demo 业务域」库连接 |
| `KEYSTONE_REDIS_HOST` / `_PORT` / `_PWD` / `_DB` | `spring.redis.*` | Redis 连接 |
| `LOG_PATH` | `logback-local.xml` | 文件日志目录，默认 `./logs` |

### Maven Profile 打包

`pom.xml` 提供 `local`（默认激活）/ `dev` / `fat` / `pro` 四个 profile，各自设置 `profileActive`。打包时经 resource 过滤把 `application.yml` 里的 `@profileActive@` 替换为对应环境名：

```bash
mvn clean package -P dev     # 得到 spring.profiles.active: ${PROFILE:dev}
```

- 运行期 `PROFILE` 环境变量优先级最高，可临时覆盖打进包的默认 profile。
- resource 过滤**只对** `yml/yaml/properties/xml` 生效，其余资源（图片/字体/JS/HTML 模板）原样拷贝，避免二进制被破坏。
- 各环境差异配置放于 `resources/config/application-{profile}.yml`。

## 动态多数据源

机制（与原项目一致）：数据源 = `环境连接参数 × 业务域库名`，启动时笛卡尔积注册，**数据源名 = 环境名 + 业务域后缀**。

| 文件 | 说明 |
| --- | --- |
| `DynamicDataSourceProperties`（`keystone.datasource.*`） | 环境连接信息 + 业务域→库名 映射 |
| `MultiDataSourceRegister` | `@PostConstruct` 动态注册到 `DynamicRoutingDataSource`（强转前做类型校验） |
| `DataSourceNames` | 数据源名称常量（`localDemo`、`devDemo`…） |
| `AbstractDemoTagService` + `LocalDemoTagService` | 抽象基类 + 各环境子类，基类提供通用查询，子类 `@DS` 指定数据源 |

使用方式：
```java
@Service
@DS(DataSourceNames.LOCAL_DEMO)   // 切到动态数据源 localDemo（keystone_demo 库）
public class LocalDemoTagService extends AbstractDemoTagService { }
```
主数据源（`master`/keystone）上的 service 不加 `@DS`，走 `spring.datasource.dynamic.primary` 默认源，实现两者自由切换。

**Demo**：`首页 → 多数据源演示`（`/demo/datasource`，接口 `/api/v1/demo/datasource`）。一次请求内先后查询主库 `sys_demo_item` 与第二库 `sys_demo_tag`，直观展示切换。本机演示需两个库：`keystone` 与 `keystone_demo`（均见 `doc/schema.sql`）。

### 扩展：环境策略工厂（按 env 自动选 Service）

当同一业务域存在多个环境数据源（如 devData/testData/prodData…）时，可按「策略模式」自动路由：

| 文件 | 说明 |
| --- | --- |
| `annotation/EnvType` + `constant/EnvConstants` | 声明环境类型（LOCAL=1/DEV=2/TEST=3/PROD=4） |
| `service/strategy/EnvStrategy` | 接口默认方法 `envType()` 用 `AnnotationUtils` 穿透代理读取 `@EnvType` |
| `service/strategy/DemoTagEnvService` | 环境策略接口（声明业务方法 + `dsName()`） |
| `AbstractDemoTagService` | 抽象基类：实现通用业务逻辑，`implements DemoTagEnvService` |
| `LocalDemoTagService` / `DevDemoTagService` | `@Service @DS(DataSourceNames.xxx) @EnvType(EnvConstants.xxx)` |
| `DemoTagStrategyService` | 门面：注入 `List<DemoTagEnvService>`，`toMap(EnvStrategy::envType)` 一行路由 |

用法（Controller 无任何 if/switch）：
```java
DemoTagEnvService svc = facade.getEnvService(reqEnv); // reqEnv 来自前端
svc.listTags(); // 自动在该环境的数据源执行
```
新增环境 = 新增一个带 `@DS` + `@EnvType` 的实现类，门面零改动。

本地演示中 `local` 与 `dev` 两个环境暂都指向 `keystone_demo`（Demo 业务域）同库，仅演示「按 env 选 service」的路由逻辑；要真正隔离，给 `dev` 单独建库并改写 `keystone.datasource.environments.dev` 的库名即可。

## 目录结构

```
keystone/
├── pom.xml
├── README.md
├── src/main/
│   ├── java/com/keystone/admin/
│   │   ├── KeystoneApplication.java        # 入口（@MapperScan + @EnableAsync）
│   │   ├── annotation/                     # MenuPermission / EnvType
│   │   ├── config/                          # 拦截器 / WebMvc / Redis / Cors / Jackson / 安全与数据源配置
│   │   ├── constant/ error/ exception/      # 常量 / 错误码 / 统一异常
│   │   ├── web/                             # ApiResult / ApiPageResult / 分页工具
│   │   ├── utils/                           # JwtUtils / RedisUtil / Util
│   │   ├── dto/ vo/ entity/ mapper/ service/ controller/
│   │   └── (entity/mapper/service/controller 中的 Demo* 为示例模块)
│   └── resources/
│       ├── application.yml                  # 通用配置 + keystone.security + 环境变量占位
│       ├── config/application-{local,dev}.yml  # 各环境连接（DB / Redis / JWT 开发密钥）
│       ├── logback-local.xml                # 控制台(local) + 按天滚动文件(!local)
│       ├── static/                          # layuiAdmin 前端主题（可直接改样式）
│       ├── templates/                       # FreeMarker 页面
│       └── doc/schema.sql                   # 演示表结构
```

## 接口与错误码

主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | 登录，成功签发 token 并写 HttpOnly Cookie |
| POST | `/api/v1/user/logout` | 登出，删除缓存 token 并使 Cookie 立即过期 |
| GET/POST | `/api/v1/user/getInfo` | 当前登录用户信息 |
| GET/POST | `/api/v1/menuPermission/current` | 当前用户可访问菜单权限（前端渲染菜单） |
| GET/POST | `/api/v1/demo/page` | 分页查询（`page/limit/name/status`，`limit` 上限 200） |
| POST | `/api/v1/demo/add` · `/update` · `/delete` | 新增 / 编辑 / 批量删除 |
| GET/POST | `/api/v1/demo/datasource` · `/env` | 多数据源、环境策略演示 |
| GET/POST | `/api/**`（兜底） | 未命中的接口统一返回 `4003` |

统一返回码（`error/ErrorCode`）：

| code | 含义 |
| --- | --- |
| 0 | 请求成功 |
| 1001 | 未登录或登录已过期（前端跳登录页） |
| 4000 | 请求参数非法 |
| 4001 | 请求方法不支持 |
| 4002 | 请求体错误 |
| 4003 | 请求的资源未找到 |
| 4010 | 用户无权限（严格模式） |
| 5000 | 系统异常 |

## 新增一个业务模块（范式）

以 `demo` 模块为模板，复制 5 类文件并改名：

| 文件 | 说明 |
| --- | --- |
| `DemoItem` Entity + `DemoItemMapper`（MyBatis-Plus） | 表映射 + 基础 CRUD |
| `DemoItemService`（ServiceImpl） | 业务逻辑（批量删除等写操作加 `@Transactional`） |
| `DemoController`（`/api/v1/demo/**`） | REST 接口（page/add/update/delete） |
| `SystemController.demoList/demoAdd` 两条页面路由 | 返回 `demo/list`、`demo/add` 视图 |
| `templates/demo/list.html` + `add.html` | layui 列表页 + 表单页 |

步骤：

1. 复制 `DemoItem` → `MyBusiness`，改表名映射。
2. 复制 `DemoItemMapper` / `DemoItemService` / `DemoController`，替换 URL 前缀与查询条件。
3. 在 `SystemController` 加两条 `@MenuPermission(value="xxx:list", name="xxx")` 页面路由。
4. 复制 `templates/demo/list.html`、`add.html`，替换 table 的 `url` 与表单 `url`。
5. 在 `templates/index.html` 侧边栏与 `home/main.html` 快捷方式加菜单（带 `data-permission`，前端按权限渲染；`data-permission` 与注解 `value` 对应）。

## 重命名为新项目

搜索替换以下占位：

| 占位 | 说明 | 位置 |
| --- | --- | --- |
| `com.keystone.admin` | 基础包名 | 全部 Java 文件 |
| `keystone` / `Keystone` | artifactId、应用名、页面标题 | `pom.xml`、入口类、`templates/*.html` 标题 |
| `x-keystone-auth-token` | 请求头 token 字段名 | `JwtUtils`、`static/config.js`（`setter.request.tokenName`，二者必须一致） |
| `Keystone` | 系统名称 | `index.html`、`login.html` 文案 |

## 认证扩展

`AuthService.validate` 默认校验 `application.yml` 的 `keystone.security.users`。对接真实用户体系（用户表 / AD / LDAP）时，仅需修改 `AuthService` 的 `validate` 实现，token 签发、拦截器、页面框架均无需改动。

> 提示：token 当前缓存在进程内存（按 profile 隔离），重启即失效、多实例不共享。需要分布式共享/主动失效时，把 `JwtUtils` 的内存 `tokenCache` 换成 `RedisUtil` 调用即可。

## 前端说明

- 主题为 layuiAdmin（Lay-UI 开源协议，见各文件头注）。
- 页面资源统一 `/static/`（`layui`、`lib`、`style`、`tpl`、`editor`、`modules/common.js`）。
- `setter.request.tokenName` 必须与后端 `JwtUtils.TOKEN_HEADER` 一致；`setter.response.statusCode.logout`（1001）与登录拦截器返回码一致。
