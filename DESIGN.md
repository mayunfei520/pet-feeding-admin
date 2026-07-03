# 宠物喂养管理平台 — 设计文档

> **版本**: v1.0.0
> **日期**: 2026-07-02
> **作者**: PetFeeding Team

---

## 1. 项目概述

宠物上门喂养服务平台，包含管理后台 Web + 微信小程序双前端，共用同一 Spring Boot 后端。

### 1.1 核心功能
- **用户管理**：管理员 CRUD、客户管理（启用/禁用/删除）
- **宠物管理**：宠物档案 CRUD（种类、品种、年龄、体重、医疗备注）
- **喂养员管理**：申请审核（通过/拒绝）、已通过列表、评价查看
- **订单管理**：订单列表、状态过滤、分配喂养员、短信通知、取消/删除
- **评价管理**：全局评价浏览、单喂养员评价详情
- **支付记录**：支付流水查询（微信/支付宝、未支付/已支付/退款）
- **仪表盘**：统计数据概览、快捷操作入口

### 1.2 双端架构
```
┌─────────────────┐       ┌─────────────────┐
│  管理后台 Web    │       │  微信小程序      │
│  Vue 3 + Vite   │       │  微信开发者工具  │
│  :5173          │       │  :8080/miniapp   │
└────────┬────────┘       └────────┬────────┘
         │                         │
         ▼                         ▼
┌─────────────────────────────────────────┐
│         Spring Boot 后端 :8080           │
│  - /api/*         → 管理后台接口          │
│  - /api/miniapp/* → 小程序接口            │
│  H2 数据库 | JWT | Spring Security       │
└─────────────────────────────────────────┘
```

---

## 2. 技术栈

### 2.1 后端
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| MyBatis-Plus | 3.5.3.1 | ORM |
| Spring Security | 2.7.18 | 认证授权 |
| JWT (jjwt) | 0.9.1 | Token 认证 |
| Knife4j | 4.1.0 | API 文档 |
| H2 Database | — | 开发数据库 |
| Lombok | — | 代码简化 |

### 2.2 前端 — 管理后台
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5.x | 核心框架 |
| Vite | 8.x | 构建工具 |
| Element Plus | 2.14.x | UI 组件库 |
| Vue Router | 4.6.x | 路由 |
| Axios | 1.18.x | HTTP 客户端 |

### 2.3 前端 — 小程序
| 技术 | 用途 |
|------|------|
| 微信小程序原生 | 用户端交互 |
| Mock 微信登录 | 开发环境模拟 wx.login() |

---

## 3. 数据库设计

### 3.1 ER 关系图
```
users ──< pets          (一对多)
users ──< feeders       (一对一)
users ──< orders(owner) (一对多)
users ──< orders(feeder)(一对多)
users ──< reviews(owner)(一对多)
users ──< payments      (一对多)

feeders ──< orders      (一对多)
feeders ──< reviews      (一对多)

pets ──< orders         (一对多)
orders ──< reviews      (一对一)
orders ──< payments     (一对多)
```

### 3.2 表结构

#### users — 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| username | VARCHAR(50) UNIQUE | 用户名 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| role | VARCHAR(20) | OWNER / FEEDER / ADMIN |
| avatar | VARCHAR(500) | 头像 URL |
| status | VARCHAR(20) | ACTIVE / DISABLED |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### pets — 宠物表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT FK | 所属用户 |
| name | VARCHAR(50) | 宠物名 |
| species | VARCHAR(20) | CAT / DOG / OTHER |
| breed | VARCHAR(50) | 具体品种 |
| age | INT | 年龄（岁） |
| weight | DECIMAL(5,2) | 体重（kg） |
| medical_notes | VARCHAR(500) | 医疗备注 |
| vaccinated | TINYINT(1) | 是否已打疫苗 |
| image | VARCHAR(500) | 照片 URL |
| created_at | DATETIME | 创建时间 |

#### feeders — 喂养员表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| user_id | BIGINT UK | 关联用户 |
| real_name | VARCHAR(20) | 真实姓名 |
| id_card | VARCHAR(18) | 身份证号 |
| service_area | VARCHAR(100) | 服务区域 |
| experience | VARCHAR(500) | 经验描述 |
| description | VARCHAR(500) | 自我介绍 |
| certification | VARCHAR(1000) | 资质证书 URL |
| status | VARCHAR(20) | PENDING / APPROVED / REJECTED |
| rating | DECIMAL(3,2) | 评分（满分 5.0） |
| created_at | DATETIME | 创建时间 |

#### orders — 订单表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_no | VARCHAR(30) UK | 订单编号 |
| owner_id | BIGINT FK | 主人 ID |
| feeder_id | BIGINT FK | 喂养员 ID |
| pet_id | BIGINT FK | 宠物 ID |
| service_date | DATE | 服务日期 |
| service_period | VARCHAR(10) | AM / PM / EVENING |
| status | VARCHAR(20) | PENDING / ACCEPTED / IN_PROGRESS / COMPLETED / CANCELLED |
| address | VARCHAR(200) | 服务地址 |
| notes | VARCHAR(500) | 备注 |
| price | DECIMAL(10,2) | 订单金额 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### reviews — 评价表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_id | BIGINT UK FK | 关联订单 |
| owner_id | BIGINT FK | 评价人 |
| feeder_id | BIGINT FK | 被评价喂养员 |
| rating | TINYINT | 评分 1-5 |
| content | VARCHAR(1000) | 评价内容 |
| images | VARCHAR(2000) | 评价图片 URL |
| created_at | DATETIME | 创建时间 |

#### payments — 支付记录表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| order_id | BIGINT FK | 关联订单 |
| user_id | BIGINT FK | 付款用户 |
| amount | DECIMAL(10,2) | 支付金额 |
| pay_method | VARCHAR(20) | WECHAT / ALIPAY |
| pay_status | VARCHAR(20) | UNPAID / PAID / REFUNDED |
| transaction_id | VARCHAR(100) | 第三方交易号 |
| created_at | DATETIME | 创建时间 |

---

## 4. API 设计

### 4.1 管理后台 API

#### 用户模块 `/api/user`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/login` | 登录 | 公开 |
| POST | `/register` | 注册 | 公开 |
| GET | `/list` | 用户列表 | JWT |
| GET | `/list/{role}` | 按角色筛选 | JWT |
| PUT | `/update-status/{id}` | 启用/禁用 | JWT |
| DELETE | `/{id}` | 删除用户 | JWT |
| PUT | `/reset-password/{id}` | 重置密码 | JWT |

#### 宠物模块 `/api/pet`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/all` | 宠物列表 | JWT |
| POST | `/add` | 新增宠物 | JWT |
| PUT | `/update` | 更新宠物 | JWT |
| DELETE | `/{id}` | 删除宠物 | JWT |

#### 喂养员模块 `/api/feeder`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/list` | 喂养员列表 | JWT |
| GET | `/pending` | 待审核列表 | JWT |
| PUT | `/approve/{id}` | 审核通过 | JWT |
| PUT | `/reject/{id}` | 拒绝申请 | JWT |
| DELETE | `/{id}` | 删除喂养员 | JWT |

#### 订单模块 `/api/order`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/all` | 订单列表 | JWT |
| POST | `/create` | 创建订单 | JWT |
| GET | `/my` | 我的订单 | JWT |
| GET | `/pending` | 待接订单 | JWT |
| PUT | `/cancel/{id}` | 取消订单 | JWT |
| PUT | `/assign/{id}` | 分配喂养员 | JWT |
| DELETE | `/{id}` | 删除订单 | JWT |

#### 评价模块 `/api/review`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/all` | 评价列表 | JWT |
| GET | `/by-feeder/{id}` | 喂养员评价 | JWT |
| DELETE | `/{id}` | 删除评价 | JWT |

#### 支付模块 `/api/payment`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/all` | 支付记录 | JWT |

#### 仪表盘 `/api/dashboard`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/stats` | 统计数据 | JWT |

### 4.2 小程序 API

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/miniapp/auth` | 微信登录 |
| 宠物 | `/api/miniapp/pets` | 宠物 CRUD |
| 订单 | `/api/miniapp/orders` | 订单全流程 |
| 喂养员 | `/api/miniapp/feeders` | 申请/列表/评价 |
| 评价 | `/api/miniapp/reviews` | 创建/查询 |

### 4.3 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 4.4 认证机制
- 管理后台：用户名 + 密码 → JWT Token → localStorage
- 小程序：wx.login(code) → Mock openId → JWT Token → wx.storage
- 请求头：`Authorization: Bearer <token>`

---

## 5. 前端设计

### 5.1 架构
```
frontend/
├── src/
│   ├── main.js              # Vue 应用入口
│   ├── App.vue              # 根组件
│   ├── style.css            # 全局设计系统
│   ├── router/
│   │   └── index.js         # 路由 + 导航守卫
│   ├── utils/
│   │   ├── request.js       # Axios 封装
│   │   └── api.js           # API 模块导出
│   ├── components/
│   │   ├── Layout.vue       # 侧边栏 + 头部
│   │   └── PageTable.vue    # 通用表格组件
│   └── views/
│       ├── login/
│       │   └── LoginView.vue    # 登录页
│       ├── Dashboard.vue        # 仪表盘
│       ├── UserList.vue         # 客户管理
│       ├── AdminList.vue        # 管理员管理
│       ├── PetList.vue          # 宠物管理
│       ├── FeederList.vue       # 喂养员管理
│       ├── ReviewList.vue       # 喂养员评价
│       ├── ReviewManage.vue     # 评价管理
│       ├── OrderList.vue        # 订单管理
│       └── PaymentList.vue      # 支付记录
```

### 5.2 路由表
| 路径 | 组件 | 标题 | 分组 |
|------|------|------|------|
| `/` | Dashboard | 仪表盘 | 概览 |
| `/users` | UserList | 客户管理 | 业务管理 |
| `/admins` | AdminList | 管理员管理 | 系统管理 |
| `/pets` | PetList | 宠物管理 | 业务管理 |
| `/feeders` | FeederList | 喂养员管理 | 业务管理 |
| `/feeders/:id/reviews` | ReviewList | 喂养员评价 | 业务管理 |
| `/orders` | OrderList | 订单管理 | 业务管理 |
| `/reviews` | ReviewManage | 评价管理 | 业务管理 |
| `/payments` | PaymentList | 支付记录 | 业务管理 |

### 5.3 设计系统

#### 色彩体系
| 变量 | 值 | 用途 |
|------|-----|------|
| `--brand-primary` | `#6366f1` | 品牌主色 |
| `--brand-primary-light` | `#818cf8` | 品牌亮色 |
| `--brand-primary-dark` | `#4f46e5` | 品牌暗色 |
| `--brand-gradient` | `linear-gradient(135deg, #6366f1 → #8b5cf6 → #a78bfa)` | 品牌渐变 |
| `--color-success` | `#10b981` | 成功色 |
| `--color-warning` | `#f59e0b` | 警告色 |
| `--color-danger` | `#ef4444` | 危险色 |
| `--color-info` | `#3b82f6` | 信息色 |

#### 圆角体系
| 变量 | 值 | 用途 |
|------|-----|------|
| `--radius-sm` | `8px` | 小按钮、标签 |
| `--radius-md` | `12px` | 卡片、表格 |
| `--radius-lg` | `16px` | 弹窗、大卡片 |
| `--radius-xl` | `20px` | 登录卡片 |

#### 阴影体系
| 变量 | 值 | 用途 |
|------|-----|------|
| `--shadow-sm` | `0 1px 2px rgba(0,0,0,0.04)` | 细微层次 |
| `--shadow-md` | `0 4px 12px rgba(0,0,0,0.06)` | 卡片悬停 |
| `--shadow-lg` | `0 8px 24px rgba(0,0,0,0.08)` | 悬浮卡片 |
| `--shadow-xl` | `0 12px 40px rgba(0,0,0,0.12)` | 弹窗 |
| `--shadow-brand` | `0 4px 16px rgba(99,102,241,0.25)` | 品牌按钮 |

#### 动画体系
| 名称 | 效果 | 用途 |
|------|------|------|
| `fadeInUp` | 从下方淡入 | 页面元素入场 |
| `fadeIn` | 透明度 0→1 | 弹窗出现 |
| `slideInLeft` | 从左侧滑入 | 侧边栏 |
| `pulse` | 呼吸闪烁 | 加载状态 |
| `shimmer` | 渐变流动 | 骨架屏 |

### 5.4 页面设计

#### 登录页
- 全屏深色渐变背景 + 三个浮动光斑动画
- 毛玻璃登录卡片（backdrop-filter: blur）
- 输入框带图标 + 聚焦发光效果
- 按钮带渐变 + 悬停上浮 + 加载旋转

#### 仪表盘
- 6 张统计卡片：彩色图标 + 大号数值 + 标签
- 卡片悬停上浮 + 阴影增强
- 快捷操作区：3 张渐变图标卡片
- 右上角实时时钟

#### 列表页（通用模板 PageTable）
- 统一页面头部：标题 + 描述 + 操作区
- 可选过滤器栏
- 统一表格样式：粘性表头 + 交替行底色 + 悬停高亮
- 统一 Tag 样式：胶囊形状态标签
- 统一按钮样式：品牌色主按钮 + 描边次要按钮 + 危险描边按钮
- 空状态占位图

---

## 6. 安全设计

### 6.1 认证流程
```
用户登录 → 后端验证 → 签发 JWT → 前端存储 → 请求携带 → 过滤器校验
```

### 6.2 权限控制
| 路径 | 认证要求 |
|------|---------|
| `/api/user/login`, `/api/user/register` | 公开 |
| `/api/miniapp/auth/login` | 公开 |
| `/api/miniapp/**` (GET) | 公开（小程序读操作） |
| 其他所有接口 | 需要 JWT Token |

### 6.3 密码安全
- BCrypt 哈希加密存储
- 重置密码生成随机新密码

---

## 7. 部署说明

### 7.1 开发环境
```bash
# 后端
cd backend && mvn spring-boot:run -Dfile.encoding=UTF-8

# 前端
cd frontend && npm run dev
```

### 7.2 生产环境（规划）
| 服务 | 方案 |
|------|------|
| 后端 | Spring Boot Jar + systemd / Docker |
| 前端 | Vite build → Nginx 静态托管 |
| 数据库 | 迁移至 MySQL / PostgreSQL |
| 小程序 | Mock 登录 → 微信官方 API |

---

## 8. 版本记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-07-02 | 初始版本：完整 CRUD、双端架构、现代 UI 设计 |
