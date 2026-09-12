---
name: admin-backend-dev
description: 宠物喂养管理后台后端开发规范 — Spring Boot + MyBatis-Plus + JWT + Controller 模式
---

# 管理后台后端开发规范

这是 `pet-feeding-admin` 后端的开发规范，**注意此后端同时服务于小程序（`/api/miniapp/*`）和管理后台（`/api/*`）**。

## 项目约束

- **Java 1.8**：不可使用 9+ 特性
- **Spring Boot 2.7.18**：不是 3.x
- **MyBatis-Plus 3.5.3.1**：`ServiceImpl` 继承模式
- **数据库**：H2 文件数据库（MySQL 兼容模式），DDL 在 `db/schema.sql`
- **JWT**：`jjwt 0.9.1`（旧 API）

## 三层 Controller 模式

### 1. 管理后台 Controller（`/api/*`）
- 文件位置：`module/<domain>/controller/<Domain>Controller.java`
- 使用 `Authentication` 获取当前用户：`authentication.getPrincipal()` → userId
- 所有接口需 JWT（SecurityConfig 默认认证）

### 2. 小程序 Controller（`/api/miniapp/*`）
- 文件位置：`module/miniapp/controller/MiniApp<Domain>Controller.java`
- 从 `Authorization` header 手动提取 token：`jwtUtil.getUserIdFromToken(token)`
- GET 请求公开（SecurityConfig 放行），POST/PUT/DELETE 需 JWT

## 统一响应

```java
// 成功
R.ok(data)           // { code: 200, message: "操作成功", data: ... }
R.ok("自定义消息")    // { code: 200, message: "自定义消息", data: null }

// 失败
R.fail("错误消息")    // { code: 500, message: "错误消息", data: null }
R.fail(403, "无权操作") // { code: 403, message: "无权操作", data: null }
```

## Service 模式

```java
@Service
@RequiredArgsConstructor  // Lombok 构造注入
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {
    // 继承方法: save(), getById(), updateById(), removeById(), list(), count()
    // Lambda 查询: this.lambdaQuery().eq(Xxx::getField, value).list()
}
```

## Security 配置

需要放行新路由时修改：
```java
// SecurityConfig.java
.antMatchers("/api/xxx").permitAll()  // 完全放行
.antMatchers(HttpMethod.GET, "/api/miniapp/xxx/**").permitAll()  // 小程序读放行
```

## API 文档访问
- Knife4j: `http://localhost:8080/doc.html`
- H2 Console: `http://localhost:8080/h2-console`
