# MEMORY.md — 项目长期记忆

## 服务器部署信息
- **SSH**: ubuntu@101.42.24.114
- **部署目录**: /home/ubuntu/docker-deploy/pet-feeding-admin-docker/
- **4个容器**: pet-feeding-mysql, pet-feeding-backend, pet-feeding-frontend, pet-feeding-nginx
- **登录账号**: admin / 123456
- **MySQL root密码**: PetFeed_DB_R00t_2026!（2026-07-09 更换，原为 123456）
- **MySQL业务用户**: petfeeder / PetFeeding2024!
- **数据库**: pet_feeding

## 安全教训（2026-07-09 勒索攻击）
- MySQL 3306 端口曾暴露公网 + root 密码 `123456`，被攻击者删除数据库并勒索比特币
- 修复：MySQL 端口绑定 127.0.0.1，更换强密码，后端使用 prod profile
- **永远不要将数据库端口暴露到公网**

## 技术架构要点
- 后端：Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 + MySQL 8.0
- 前端：Vue 3 + Element Plus + Vite
- 登录接口：POST /api/user/login（不是 /auth/login）
- SecurityConfig permitAll 路径：/api/user/login, /api/user/register, /api/miniapp/**
- 前端 baseURL: /api，API 路径如 /user/login → 实际请求 /api/user/login
- application-prod.yml 使用环境变量 ${MYSQL_HOST} 等，由 docker-compose.yml 注入

## 已知配置问题
- JwtAuthFilter 认证后 Authorities 为空列表，@PreAuthorize 注解可能不生效
- CORS 配置过于宽松（addAllowedOriginPattern("*") + allowCredentials(true)）
- SecurityConfig 未放行 OPTIONS 预检请求
