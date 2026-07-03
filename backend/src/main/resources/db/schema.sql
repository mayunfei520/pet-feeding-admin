-- =====================================================
-- 宠物喂养服务平台 - 数据库建表脚本
-- =====================================================

CREATE DATABASE IF NOT EXISTS `pet_feeding`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `pet_feeding`;

-- =====================================================
-- 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `users` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)   NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(255)  NOT NULL                COMMENT '密码（BCrypt加密）',
    `phone`       VARCHAR(20)   DEFAULT NULL            COMMENT '手机号',
    `email`       VARCHAR(100)  DEFAULT NULL            COMMENT '邮箱',
    `role`        VARCHAR(20)   NOT NULL DEFAULT 'OWNER' COMMENT '角色：OWNER-宠物主人, FEEDER-喂养员, ADMIN-管理员',
    `avatar`      VARCHAR(500)  DEFAULT NULL            COMMENT '头像URL',
    `status`      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常, DISABLED-禁用',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 宠物表
-- =====================================================
CREATE TABLE IF NOT EXISTS `pets` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT        NOT NULL                COMMENT '所属用户ID',
    `name`          VARCHAR(50)   NOT NULL                COMMENT '宠物名称',
    `species`       VARCHAR(20)   NOT NULL                COMMENT '品种：CAT-猫, DOG-狗, OTHER-其他',
    `breed`         VARCHAR(50)   DEFAULT NULL            COMMENT '具体品种（如：英短、金毛）',
    `age`           INT           DEFAULT NULL            COMMENT '年龄（岁）',
    `weight`        DECIMAL(5,2)  DEFAULT NULL            COMMENT '体重（kg）',
    `medical_notes` VARCHAR(500)  DEFAULT NULL            COMMENT '医疗备注',
    `vaccinated`    TINYINT(1)    DEFAULT NULL            COMMENT '是否已打疫苗',
    `image`         VARCHAR(500)  DEFAULT NULL            COMMENT '宠物照片URL',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物表';

-- =====================================================
-- 喂养员表
-- =====================================================
CREATE TABLE IF NOT EXISTS `feeders` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT        NOT NULL                COMMENT '关联用户ID',
    `real_name`     VARCHAR(20)   NOT NULL                COMMENT '真实姓名',
    `id_card`       VARCHAR(18)   NOT NULL                COMMENT '身份证号',
    `service_area`  VARCHAR(100)  NOT NULL                COMMENT '服务区域（如：朝阳区）',
    `experience`    VARCHAR(500)  DEFAULT NULL            COMMENT '经验描述',
    `description`   VARCHAR(500)  DEFAULT NULL            COMMENT '自我介绍',
    `certification` VARCHAR(1000) DEFAULT NULL            COMMENT '资质证书URL（多个用逗号分隔）',
    `status`        VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
    `rating`        DECIMAL(3,2)  DEFAULT 5.00            COMMENT '评分（满分5.0）',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_service_area` (`service_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喂养员表';

-- =====================================================
-- 订单表
-- =====================================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`       VARCHAR(30)   NOT NULL               COMMENT '订单编号',
    `owner_id`       BIGINT        NOT NULL               COMMENT '宠物主人ID',
    `feeder_id`      BIGINT        DEFAULT NULL           COMMENT '喂养员ID',
    `pet_id`         BIGINT        NOT NULL               COMMENT '宠物ID',
    `service_date`   DATE          NOT NULL               COMMENT '服务日期',
    `service_period` VARCHAR(10)   NOT NULL               COMMENT '服务时段：AM-上午, PM-下午, EVENING-晚上',
    `status`         VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待接单, ACCEPTED-已接单, IN_PROGRESS-进行中, COMPLETED-已完成, CANCELLED-已取消',
    `address`        VARCHAR(200)  NOT NULL               COMMENT '服务地址',
    `notes`          VARCHAR(500)  DEFAULT NULL           COMMENT '备注',
    `price`          DECIMAL(10,2) NOT NULL               COMMENT '订单金额',
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_feeder_id` (`feeder_id`),
    KEY `idx_status` (`status`),
    KEY `idx_service_date` (`service_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 评价表
-- =====================================================
CREATE TABLE IF NOT EXISTS `reviews` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`   BIGINT        NOT NULL               COMMENT '关联订单ID',
    `owner_id`   BIGINT        NOT NULL               COMMENT '评价人ID（宠物主人）',
    `feeder_id`  BIGINT        NOT NULL               COMMENT '被评价喂养员ID',
    `rating`     TINYINT       NOT NULL               COMMENT '评分 1-5',
    `content`    VARCHAR(1000) DEFAULT NULL           COMMENT '评价内容',
    `images`     VARCHAR(2000) DEFAULT NULL           COMMENT '评价图片（多个URL用逗号分隔）',
    `created_at` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_feeder_id` (`feeder_id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- =====================================================
-- 支付记录表
-- =====================================================
CREATE TABLE IF NOT EXISTS `payments` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`       BIGINT        NOT NULL               COMMENT '关联订单ID',
    `user_id`        BIGINT        NOT NULL               COMMENT '付款用户ID',
    `amount`         DECIMAL(10,2) NOT NULL               COMMENT '支付金额',
    `pay_method`     VARCHAR(20)   NOT NULL               COMMENT '支付方式：WECHAT-微信, ALIPAY-支付宝',
    `pay_status`     VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付, PAID-已支付, REFUNDED-已退款',
    `transaction_id` VARCHAR(100)  DEFAULT NULL           COMMENT '第三方交易号',
    `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';
