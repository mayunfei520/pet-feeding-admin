# 后端优化 & 前后端闭环审计报告

> 审计时间：2026-07-10 ｜ 范围：pet-feeding-admin 后端(12 controller) + 前端(9 页面) + 安全配置
> 审计方式：静态代码比对（前端 api.js/各页面调用 ↔ 后端 @RequestMapping 路由）+ 安全配置审阅

---

## 一、结论速览

| 维度 | 结论 | 说明 |
|------|------|------|
| 前后端联动 | ✅ 全打通 | 前端所有 API 调用均有后端实现，零 404 断点 |
| 功能闭环 | ✅ 成立 | Admin 管理端 + 小程序端各自闭环，创建类操作职责分离 |
| 后端安全优化 | ⚠️ 有隐患 | 3 项 P0 安全隐患（权限未生效 / CORS 过宽 / OPTIONS 未放行） |
| 后端工程化 | ⚠️ 可优化 | 4 项（无分页 / Map 当 DTO / 建表依赖手动 / prod 噪音） |

---

## 二、前后端接口对照（联动验证）

前端 `src/utils/api.js` 暴露的方法 ↔ 后端真实端点：

| 前端调用 | 后端端点 | 状态 |
|----------|----------|------|
| userApi.login | POST `/api/user/login` | ✅ |
| userApi.register | POST `/api/user/register` | ✅ |
| userApi.listByRole(role,gender) | GET `/api/user/list?role&gender` | ✅ |
| userApi.update(id,data) | PUT `/api/user/{id}` | ✅ |
| userApi.updateStatus(id,status) | PUT `/api/user/{id}/status` | ✅ |
| userApi.remove(id) | DELETE `/api/user/{id}` | ✅ |
| userApi.resetPassword(id) | POST `/api/user/{id}/reset-password` | ✅ |
| petApi.list / remove | GET `/api/pet/all` / DELETE `/api/pet/{id}` | ✅ |
| feederApi.list/pending/approve/reject/remove | `/api/feeder/*` | ✅ |
| orderApi.list/cancel/assign/remove | `/api/order/*` | ✅ |
| reviewApi.list/remove | `/api/review/*` | ✅ |
| paymentApi.list | GET `/api/payment/all` | ✅ |
| dashboardApi.stats | GET `/api/dashboard/stats` | ✅ |

页面实际调用核对（Grep 结果）：LoginView / PetList / Dashboard / ReviewList / AdminList / FeederList / UserList / OrderList / PaymentList / ReviewManage —— 全部命中上表接口，**无孤儿调用、无 404 端点**。

---

## 三、闭环完整性分析

### 已闭环（设计合理）
- **Admin 端**：用户/客户管理（含新增、性别编辑、启用禁用、删除）、喂养员审核、订单分配/取消、评价管理、支付查看、仪表盘统计 —— 管理操作闭环完整。
- **小程序端**：宠物增删改、订单创建/接单/完成、评价、登录注册（含短信验证码）—— 由 `MiniApp*` controller 提供完整 CRUD，职责清晰。

### 半截点（属设计取舍，非 Bug）
- **Pet 在 Admin 端只有 list/delete**，无新增/编辑按钮（前端 `petApi` 未封装 add/update，尽管后端 `PetController` 有 `add`/`update`）。→ 宠物由小程序端创建，Admin 仅管理，符合业务流。
- **Order 在 Admin 端无"创建"入口**（前端 `orderApi` 无 create，`OrderController` 有 `POST /api/order`）。→ 订单由小程序端发起，Admin 仅分配/取消。

> 结论：这是"管理端 vs 用户端"的职责分离，不是断裂的闭环，无需强制补齐。

---

## 四、后端待优化清单（按优先级）

### 🔴 P0 — 安全隐患（建议必修）

**1. 角色权限实际上未生效**
- 位置：`security/filter/JwtAuthFilter.java:42`
- 现状：`new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList())` —— Authorities 传空列表。
- 后果：`SecurityConfig` 只用 `.anyRequest().authenticated()`，未做角色级 `@PreAuthorize`；但即便将来加，`@PreAuthorize("hasRole('ADMIN')")` 也会因 Authorities 为空而**一律拒绝**。当前所有登录用户（OWNER/FEEDER/ADMIN）都能调用任意管理接口，无越权防护。
- 修复：登录时把用户 role 写入 Token，过滤器还原为 `List.of(new SimpleGrantedAuthority("ROLE_" + role))`。

**2. CORS 配置过于宽松**
- 位置：`common/config/CorsConfig.java:18-21`
- 现状：`addAllowedOriginPattern("*")` + `setAllowCredentials(true)` —— 规范上这是非法组合（带凭据的跨域不能用通配源），且等同于允许任意站点携带凭证访问。
- 修复：Admin 前端经 nginx 同源部署，CORS 本可不需要；若保留，应限定具体域名（如 `https://mayunfei.asia`）并去掉 credentials。

**3. OPTIONS 预检未显式放行**
- 位置：`security/config/SecurityConfig.java:37-49`
- 现状：未在 `authorizeRequests` 中放行 `HttpMethod.OPTIONS`。预检依赖 CorsFilter（servlet 层）兜底，脆弱。
- 修复：增加 `.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()`。

### 🟡 P1 — 工程化优化（建议做）

**4. 列表接口无分页**
- 全部 `list/all` 返回全量数据。当前数据量小无碍，规模上来后需加分页（`PageHelper` / MyBatis-Plus `Page`）。

**5. 更新接口用 `Map<String,String>` 当 DTO**
- 位置：`UserController.updateProfile` / `updateStatus` 用 `Map` 接收。
- 风险：无类型约束、无校验、Swagger 不可读。建议改为 `UpdateProfileDTO`（`@NotBlank` 校验）。

**6. prod 环境依赖手动建表**
- 位置：`common/config/DataSourceInitializer.java` 标注 `@Profile("!prod")` → prod 不自动建表，依赖人工执行 `init_prod_db.sql`。
- 风险：MySQL 数据卷一旦丢失（如本次被勒索重建），需手工补建表，易遗漏。建议 prod 也走 Flyway/Liquibase 或保留初始化 SQL 自动执行。

**7. prod 仍打印 dev 默认密码**
- 现象：后端启动日志偶现 "Using generated security password ... for development use only"。
- 原因：Spring Security 默认 user 在 prod 仍被创建（仅作噪音，不影响 JWT 登录）。建议排除 `UserDetailsServiceAutoConfiguration`。

---

## 五、建议的优化路线

| 优先级 | 动作 | 工作量 | 影响 |
|--------|------|--------|------|
| P0-1 | JWT 写入 role，过滤器还原 Authorities | 小 | 修复越权防护地基 |
| P0-2 | CORS 限定域名/去 credentials | 小 | 关闭跨域暴露面 |
| P0-3 | SecurityConfig 放行 OPTIONS | 极小 | 预检更稳健 |
| P1-4 | 列表分页 | 中 | 可扩展性 |
| P1-5 | Map→DTO | 小 | 代码健壮性 |
| P1-6 | prod 自动建表 | 中 | 部署自愈 |
| P1-7 | 关闭 dev 密码 | 极小 | 日志洁净 |

> 联动与闭环本身已达标；以上优化属于"让后端更稳、更安全的打磨"，不是补断点。
