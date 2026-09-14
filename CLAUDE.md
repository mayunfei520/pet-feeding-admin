# 宠物喂养管理后台 (pet-feeding-admin)

## 项目概述

宠物上门喂养平台的管理后台，包含 Spring Boot 后端 + Vue 3 前端。

**⚠️ 此后端同时服务于两个前端**：管理后台 Web 和小程序（`pet-feeding-miniapp`）。

## 项目结构

```
pet-feeding-admin/
├── backend/                          # Spring Boot 2.7.18 + Java 1.8
│   ├── pom.xml                       # Maven 配置
│   └── src/main/
│       ├── resources/
│       │   ├── application.yml       # 主配置 (port 8080, knife4j)
│       │   ├── application-dev.yml   # 开发配置 (H2, JWT, MyBatis-Plus)
│       │   └── db/migration/         # Flyway 迁移脚本 (V1__init, V2__add_im...)
│       └── java/com/petfeeding/platform/
│           ├── PetFeedingApplication.java
│           ├── common/               # 通用组件
│           │   ├── config/           # CorsConfig, MybatisPlusConfig, MetaObjectHandlerConfig, IdCardMigrationRunner
│           │   ├── exception/        # BusinessException, GlobalExceptionHandler
│           │   └── result/R.java     # 统一响应 {code, message, data}
│           ├── security/             # Spring Security + JWT
│           │   ├── config/SecurityConfig.java
│           │   ├── filter/JwtAuthFilter.java
│           │   └── util/JwtUtil.java
│           └── module/               # 业务模块（每模块含管理后台 + 小程序两套 Controller）
│               ├── user/             # 用户模块 (UserController + MiniAppAuthController；service含MiniAppUserService)
│               ├── pet/              # 宠物模块 (PetController + MiniAppPetController)
│               ├── feeder/           # 喂养员模块 (FeederController + MiniAppFeederController)
│               ├── order/            # 订单模块 (OrderController + MiniAppOrderController)
│               ├── review/           # 评价模块 (ReviewController + MiniAppReviewController)
│               ├── payment/          # 支付模块
│               ├── dashboard/        # 仪表盘统计
│               ├── sms/              # 短信服务
│               └── im/               # IM 模块 (MiniAppImController)
│
└── frontend/                         # Vue 3 + Vite 管理后台
    ├── index.html
    ├── vite.config.js                # Vite 配置 (port 5173, proxy /api→8080)
    ├── package.json
    └── src/
        ├── main.js                   # 入口 (createApp + router)
        ├── App.vue                   # 根组件 <router-view>
        ├── style.css                 # 全局样式
        ├── router/index.js           # 路由表 + beforeEach 鉴权
        ├── utils/
        │   ├── request.js            # axios 封装 (baseURL=/api, 自动JWT, 错误处理)
        │   └── api.js                # API 模块 (userApi, petApi, feederApi, orderApi, reviewApi, paymentApi, dashboardApi)
        ├── components/
        │   └── Layout.vue            # 侧边栏 + 顶栏 + 内容区布局
        └── views/
            ├── login/LoginView.vue   # 登录页
            ├── Dashboard.vue         # 仪表盘
            ├── UserList.vue          # 客户管理
            ├── AdminList.vue         # 管理员管理
            ├── PetList.vue           # 宠物管理
            ├── FeederList.vue        # 喂养员管理
            ├── ReviewList.vue        # 喂养员评价
            ├── ReviewManage.vue       # 评价管理
            ├── OrderList.vue         # 订单管理
            └── PaymentList.vue       # 支付记录
```

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| MyBatis-Plus | 3.5.3.1 | ORM / 代码生成 |
| Spring Security | 2.7.18 | 认证和授权 |
| JWT (jjwt) | 0.9.1 | Token 认证 |
| Knife4j | 4.1.0 | API 文档 (Swagger) |
| H2 Database | — | 开发环境数据库（文件存储） |
| Lombok | — | 简化 Java 代码 |

### 前端
| 技术 | 说明 |
|------|------|
| Vue 3 | Composition API (`<script setup>`) |
| Vue Router | 路由 + 导航守卫 |
| Vite | 构建工具 |
| Element Plus | UI 组件库 |
| Axios | HTTP 客户端 |

## API 架构

### 双线路由

```
                    ┌──────────────────────┐
                    │   Spring Boot :8080   │
                    └──────────┬───────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                                      ▼
    /api/miniapp/*                          /api/*
    (小程序 API)                           (管理后台 API)
    - 认证: GET公开, 写需JWT                - 认证: login/register公开
    - 控制器: MiniApp*Controller            - 控制器: User/Pet/Feeder/Order...Controller
    - token来源: wx.login()→Mock微信登录      - token来源: 用户名密码登录
```

### 管理后台 Controller 路由

| Controller | 路径前缀 | 主要接口 |
|------------|---------|---------|
| UserController | `/api/user` | register, login, list, updateStatus, delete |
| PetController | `/api/pet` | listAll, add, update, delete |
| FeederController | `/api/feeder` | list, pending, approve, reject |
| OrderController | `/api/order` | listAll, create, my, pending, cancel, assign |
| ReviewController | `/api/review` | listAll, byFeeder, delete |
| PaymentController | `/api/payment` | listAll |
| DashboardController | `/api/dashboard` | stats |

### 小程序 Controller 路由

| Controller | 路径前缀 | 主要接口 |
|------------|---------|---------|
| MiniAppAuthController | `/api/miniapp/auth` | login (POST, 公开) |
| MiniAppPetController | `/api/miniapp/pets` | list, add, update, delete |
| MiniAppOrderController | `/api/miniapp/orders` | list, create, pending, accept, complete, cancel |
| MiniAppFeederController | `/api/miniapp/feeders` | list, apply, reviews |
| MiniAppReviewController | `/api/miniapp/reviews` | create, byFeeder |

## SecurityConfig 规则

```java
// 放行（无需JWT）
"/api/user/login", "/api/user/register"
"/api/miniapp/**"            // 小程序全部放行，鉴权在 Controller 层手写（待重构）
"/doc.html", "/webjars/**", "/v3/api-docs/**"

// 管理接口需 ADMIN 角色
.antMatchers("/api/user/**", "/api/feeder/**", "/api/order/**",
             "/api/review/**", "/api/payment/**", "/api/dashboard/**", "/api/pet/**").hasRole("ADMIN")
.anyRequest().authenticated()  // 其余需认证
```

## 数据库

使用 H2 文件数据库（开发环境），由 **Flyway** 管理建表与版本迁移（`src/main/resources/db/migration/`）：
- `V1__init.sql` — 初始化 7 张核心表
- `V2__add_im_tables_and_feeder_reject_reason.sql` — IM 会话/消息表 + 喂养员驳回原因列

数据表：
- **users** — 用户表（username, password, role, status）
- **pets** — 宠物表（user_id, name, species, breed, age, weight）
- **feeders** — 喂养员表（user_id, real_name, service_area, status, rating）
- **orders** — 订单表（order_no, owner_id, feeder_id, pet_id, service_date, service_period, status, price）
- **reviews** — 评价表（order_id, owner_id, feeder_id, rating, content）
- **payments** — 支付表（order_id, user_id, amount, pay_method, pay_status）
- **conversations** / **messages** — IM 会话与消息表（V2 新增）
- **sms_logs** — 短信日志表

> ⚠️ 索引命名：H2 要求索引名**全局唯一**（不同于 MySQL 仅要求表内唯一），migration 中的二级索引须带表前缀（如 `idx_feeders_status`）。

H2 Web Console：`http://localhost:8080/h2-console`（JDBC URL: `jdbc:h2:file:./data/pet_feeding`）

## 关联项目

- **`D:\Workspace\pet-feeding-miniapp`** — 微信小程序，调用 `/api/miniapp/*` 接口
- 两个项目共享同一个后端，修改 API 时需同步更新对应前端的 `utils/api.js`

## 启动方式

### 后端
```bash
cd backend
mvn spring-boot:run -Dfile.encoding=UTF-8 -Dmaven.test.skip=true
# ⚠️ 必须加 -Dmaven.test.skip=true：测试代码用了 Java 10+ 的 var，但项目是 Java 1.8，编译测试会失败
# 启动后访问: http://localhost:8080
# API 文档: http://localhost:8080/doc.html
```

### 前端
```bash
cd frontend
npm run dev
# 启动后访问: http://localhost:5173
# Vite 自动代理 /api → :8080
```

### 小程序
在微信开发者工具中打开 `D:\Workspace\pet-feeding-miniapp`，需确保后端 :8080 已启动。

## 开发注意事项

- **统一响应格式**：`R<T> { code: 200, message: "xxx", data: T }`，前端 `request.js` 检查 `code !== 200` 视为错误
- **JWT 存储**：admin 前端存 `localStorage`，小程序存 `wx.storage`
- **H2 数据库**：文件存储在 `backend/data/` 目录，删掉即重置
- **CORS**：后端 `CorsConfig` 已允许所有来源（`allowedOriginPatterns("*")`）
- **小程序 Mock 登录**：`MiniAppUserService` 用 `openId = "wx_" + code.hashCode()` 模拟，生产需对接微信 API
- **Lombok**：需 IDE 安装 Lombok 插件
- **Java 版本**：1.8，注意不要使用高版本特性
