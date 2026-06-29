# 宠物喂养平台设计说明

本文件用于随代码仓库维护平台结构说明，和知识库中的设计文档保持一致。

## 1. 页面清单

### 1.1 小程序页面

| 页面路径 | 页面名称 | 角色 | 说明 |
| --- | --- | --- | --- |
| `pages/login/login` | 登录/注册 | OWNER/FEEDER | 平台统一账号注册与密码登录 |
| `pages/index/index` | 首页 | OWNER/FEEDER/ADMIN说明 | 角色化首页入口 |
| `pages/orders/list` | 订单列表 | OWNER/FEEDER | 主人查看我的订单，喂养员查看接单列表 |
| `pages/mine/index` | 我的 | OWNER/FEEDER/ADMIN说明 | 角色化个人中心 |
| `pages/pets/list` | 我的宠物 | OWNER | 宠物列表 |
| `pages/pets/add/add` | 添加/编辑宠物 | OWNER | 宠物资料维护 |
| `pages/feeders/list` | 喂养员列表 | OWNER/FEEDER/ADMIN浏览 | 查看喂养员展示 |
| `pages/feeders/detail/detail` | 喂养员详情 | OWNER/FEEDER/ADMIN浏览 | 查看喂养员详情和评价 |
| `pages/orders/create/create` | 预约喂养 | OWNER | 创建订单 |
| `pages/orders/detail/detail` | 订单详情 | OWNER/FEEDER | 查看并操作订单 |
| `pages/reviews/create/create` | 创建评价 | OWNER | 服务完成后评价 |
| `pages/feeder/apply/apply` | 申请成为喂养员 | OWNER | 喂养员认证申请 |

### 1.2 后台页面

| 路由 | 页面名称 | 说明 |
| --- | --- | --- |
| `/login` | 登录 | 后台管理员登录 |
| `/` | 仪表盘 | 平台整体统计 |
| `/users` | 客户管理 | 客户列表、状态、删除、统计展示 |
| `/admins` | 管理员管理 | 管理员新增、状态管理、删除 |
| `/pets` | 宠物管理 | 宠物后台总览 |
| `/feeders` | 喂养员管理 | 审核、删除、查看资料 |
| `/feeders/:id/reviews` | 喂养员评价 | 按喂养员查看评价 |
| `/orders` | 订单管理 | 订单筛选、取消、分配 |
| `/reviews` | 评价管理 | 平台评价管理 |
| `/payments` | 支付记录 | 支付记录查看 |

## 2. 接口清单

### 2.1 小程序接口

- `POST /api/miniapp/auth/login`
- `POST /api/miniapp/auth/register`
- `POST /api/miniapp/auth/password-login`
- `GET /api/miniapp/pets`
- `POST /api/miniapp/pets`
- `PUT /api/miniapp/pets/{id}`
- `DELETE /api/miniapp/pets/{id}`
- `GET /api/miniapp/feeders`
- `POST /api/miniapp/feeders`
- `GET /api/miniapp/feeders/{id}/reviews`
- `GET /api/miniapp/orders`
- `GET /api/miniapp/orders/pending`
- `POST /api/miniapp/orders`
- `PUT /api/miniapp/orders/{id}/accept`
- `PUT /api/miniapp/orders/{id}/start`
- `PUT /api/miniapp/orders/{id}/complete`
- `PUT /api/miniapp/orders/{id}/cancel`
- `POST /api/miniapp/reviews`
- `GET /api/miniapp/reviews/feeder/{feederId}`

### 2.2 后台接口

- `GET /api/dashboard/stats`
- `POST /api/user/register`
- `POST /api/user/login`
- `GET /api/user/list`
- `PUT /api/user/{id}/status`
- `DELETE /api/user/{id}`
- `GET /api/feeder`
- `GET /api/feeder/pending`
- `PUT /api/feeder/{id}/approve`
- `PUT /api/feeder/{id}/reject`
- `DELETE /api/feeder/{id}`
- `GET /api/order/all`
- `GET /api/order/my`
- `GET /api/order/pending`
- `POST /api/order`
- `PUT /api/order/{id}/cancel`
- `PUT /api/order/{id}/assign`
- `GET /api/pet/all`
- `POST /api/pet`
- `PUT /api/pet/{id}`
- `DELETE /api/pet/{id}`
- `GET /api/review/all`
- `GET /api/review/feeder/{feederId}`
- `DELETE /api/review/{id}`
- `GET /api/payment/all`

## 3. 状态流转图

### 3.1 用户身份流转

```mermaid
flowchart LR
  A["注册平台账号"] --> B["默认身份: OWNER"]
  B --> C["提交喂养员认证申请"]
  C --> D["PENDING 待审核"]
  D --> E["APPROVED 审核通过"]
  D --> F["REJECTED 审核拒绝"]
  E --> G["开通 FEEDER 能力"]
```

### 3.2 订单状态流转

```mermaid
flowchart LR
  A["PENDING 待接单"] --> B["ACCEPTED 已接单"]
  B --> C["IN_PROGRESS 进行中"]
  B --> D["COMPLETED 已完成"]
  C --> D["COMPLETED 已完成"]
  A --> E["CANCELLED 已取消"]
```

## 4. 数据表关系说明

### 4.1 核心表

| 表名 | 实体 | 说明 |
| --- | --- | --- |
| `users` | `User` | 平台统一用户表 |
| `pets` | `Pet` | 宠物资料表 |
| `feeders` | `Feeder` | 喂养员认证与展示资料 |
| `orders` | `Order` | 喂养服务订单 |
| `reviews` | `Review` | 服务评价 |
| `payments` | `Payment` | 支付记录 |
| `sms_logs` | `SmsLog` | 短信通知日志 |

### 4.2 表关系

```mermaid
erDiagram
  USERS ||--o{ PETS : owns
  USERS ||--o{ FEEDERS : applies
  USERS ||--o{ ORDERS : owner_places
  USERS ||--o{ REVIEWS : owner_writes
  USERS ||--o{ PAYMENTS : pays
  PETS ||--o{ ORDERS : belongs_to
  FEEDERS ||--o{ SMS_LOGS : notified
  ORDERS ||--o{ REVIEWS : generates
  ORDERS ||--o{ PAYMENTS : paid_by
  ORDERS ||--o{ SMS_LOGS : triggers
```
