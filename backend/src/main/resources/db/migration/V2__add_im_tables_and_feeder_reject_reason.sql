-- =====================================================
-- V2: 补建 IM 会话/消息表 + 喂养员驳回原因列
-- 实体已存在（module/im/entity）但 V1 未建对应表；
-- Feeder.rejectReason 字段已存在但 V1 feeders 表遗漏该列。
-- =====================================================

-- 会话表
CREATE TABLE IF NOT EXISTS conversations (
    id                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_id          BIGINT       DEFAULT NULL            COMMENT '关联订单ID',
    owner_id          BIGINT       NOT NULL                COMMENT '宠物主人ID',
    feeder_id         BIGINT       NOT NULL                COMMENT '喂养员ID',
    last_message      VARCHAR(500) DEFAULT NULL            COMMENT '最后一条消息',
    last_message_time DATETIME     DEFAULT NULL            COMMENT '最后消息时间',
    owner_unread      INT          DEFAULT 0               COMMENT '主人未读数',
    feeder_unread     INT          DEFAULT 0               COMMENT '喂养员未读数',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversations_order_id (order_id),
    KEY idx_conversations_owner_feeder (owner_id, feeder_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    conversation_id BIGINT        NOT NULL                COMMENT '关联会话ID',
    sender_id       BIGINT        NOT NULL                COMMENT '发送者ID',
    sender_role     VARCHAR(20)   NOT NULL                COMMENT '发送者角色 OWNER/FEEDER',
    type            VARCHAR(20)   NOT NULL DEFAULT 'TEXT' COMMENT '消息类型 TEXT/IMAGE',
    content         VARCHAR(2000) DEFAULT NULL            COMMENT '消息内容',
    is_read         TINYINT(1)    DEFAULT 0               COMMENT '是否已读',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 喂养员表补充驳回原因列（V1 遗漏，对应 Feeder.rejectReason）
ALTER TABLE feeders ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因';
