-- =============================================
-- CICPA 注册会计师每日学习助手 - 数据库建表脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS yudao_cpa DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yudao_cpa;

-- 1. 考点知识库表
CREATE TABLE cpa_knowledge_point (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考点ID',
    subject         TINYINT NOT NULL COMMENT '科目：1会计 2审计 3财管 4经济法 5税法 6战略',
    chapter         VARCHAR(100) NOT NULL COMMENT '章节名称',
    title           VARCHAR(200) NOT NULL COMMENT '考点标题',
    content         TEXT COMMENT '考点核心内容摘要',
    importance      TINYINT DEFAULT 3 COMMENT '重要程度 1-5',
    frequency       TINYINT DEFAULT 3 COMMENT '考试频率 1-5',
    keywords        VARCHAR(500) COMMENT '关键词，逗号分隔，用于新闻匹配',
    parent_id       BIGINT DEFAULT 0 COMMENT '父考点ID，0为顶级',
    sort            INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 0 COMMENT '状态 0正常 1停用',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_subject (subject),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB COMMENT='注会考点知识库';

-- 2. 历年真题表
CREATE TABLE cpa_exam_question (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '真题ID',
    subject         TINYINT NOT NULL COMMENT '科目',
    year            INT NOT NULL COMMENT '考试年份',
    question_type   TINYINT NOT NULL COMMENT '题型：1单选 2多选 3计算 4综合 5简答',
    content         TEXT NOT NULL COMMENT '题目内容',
    options         TEXT COMMENT '选项JSON（选择题时使用）',
    answer          TEXT NOT NULL COMMENT '标准答案',
    analysis        TEXT COMMENT '解析',
    knowledge_point_ids VARCHAR(500) COMMENT '关联考点ID列表，逗号分隔',
    difficulty      TINYINT DEFAULT 3 COMMENT '难度 1-5',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_subject_year (subject, year),
    INDEX idx_question_type (question_type)
) ENGINE=InnoDB COMMENT='注会历年真题';

-- 3. 每日财经新闻表
CREATE TABLE cpa_daily_news (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '新闻ID',
    title           VARCHAR(300) NOT NULL COMMENT '新闻标题',
    summary         TEXT COMMENT '新闻摘要',
    content         TEXT COMMENT '新闻正文',
    source_url      VARCHAR(500) COMMENT '来源URL',
    source_name     VARCHAR(100) COMMENT '来源名称',
    publish_date    DATE NOT NULL COMMENT '新闻发布日期',
    fetch_date      DATE NOT NULL COMMENT '抓取日期',
    used            BIT(1) DEFAULT 0 COMMENT '是否已使用',
    used_date       DATE COMMENT '使用日期',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_fetch_date (fetch_date),
    INDEX idx_used (used)
) ENGINE=InnoDB COMMENT='每日财经新闻';

-- 4. 每日推送内容表（核心聚合表）
CREATE TABLE cpa_daily_push (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '推送ID',
    push_date       DATE NOT NULL COMMENT '推送日期',
    news_id         BIGINT COMMENT '关联新闻ID',
    knowledge_point_id BIGINT NOT NULL COMMENT '关联考点ID',
    subject         TINYINT NOT NULL COMMENT '科目',
    title           VARCHAR(300) NOT NULL COMMENT '推送标题',
    news_summary    TEXT COMMENT '新闻要点',
    point_explain   TEXT COMMENT '考点讲解内容（AI生成）',
    question_ids    VARCHAR(500) COMMENT '关联真题ID列表',
    question_explain TEXT COMMENT '真题讲解内容（AI生成）',
    mnemonic        TEXT COMMENT '记忆辅助：口诀、表格等（AI生成，Markdown格式）',
    mindmap_data    TEXT COMMENT '思维导图数据（JSON格式）',
    push_status     TINYINT DEFAULT 0 COMMENT '推送状态 0待推送 1已推送 2推送失败',
    push_time       DATETIME COMMENT '实际推送时间',
    ai_model        VARCHAR(50) COMMENT '使用的AI模型',
    ai_token_used   INT DEFAULT 0 COMMENT 'AI消耗的token数',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    UNIQUE INDEX uk_push_date (push_date),
    INDEX idx_subject (subject),
    INDEX idx_knowledge_point_id (knowledge_point_id)
) ENGINE=InnoDB COMMENT='每日推送内容';

-- 5. 考点覆盖记录表（去重追踪）
CREATE TABLE cpa_point_coverage (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    knowledge_point_id BIGINT NOT NULL COMMENT '考点ID',
    push_id         BIGINT NOT NULL COMMENT '推送ID',
    push_date       DATE NOT NULL COMMENT '推送日期',
    coverage_round  INT DEFAULT 1 COMMENT '轮次（第几轮复习到该考点）',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    UNIQUE INDEX uk_point_round (knowledge_point_id, coverage_round),
    INDEX idx_push_date (push_date)
) ENGINE=InnoDB COMMENT='考点覆盖记录';

-- 6. AI对话会话表
CREATE TABLE cpa_chat_conversation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    title           VARCHAR(200) COMMENT '会话标题',
    subject         TINYINT COMMENT '关联科目',
    knowledge_point_id BIGINT COMMENT '关联考点ID',
    model           VARCHAR(50) COMMENT 'AI模型',
    system_prompt   TEXT COMMENT '系统提示词',
    message_count   INT DEFAULT 0 COMMENT '消息数量',
    pinned          BIT(1) DEFAULT 0 COMMENT '是否置顶',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='AI对话会话';

-- 7. AI对话消息表
CREATE TABLE cpa_chat_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    role            VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    content         TEXT NOT NULL COMMENT '消息内容',
    tokens          INT DEFAULT 0 COMMENT 'token数',
    model           VARCHAR(50) COMMENT '模型',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB COMMENT='AI对话消息';

-- 8. 用户笔记表
CREATE TABLE cpa_user_note (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '笔记ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    subject         TINYINT COMMENT '科目',
    knowledge_point_id BIGINT COMMENT '关联考点ID',
    push_id         BIGINT COMMENT '关联推送ID',
    title           VARCHAR(200) NOT NULL COMMENT '笔记标题',
    content         TEXT COMMENT '笔记内容（Markdown）',
    tags            VARCHAR(500) COMMENT '标签，逗号分隔',
    starred         BIT(1) DEFAULT 0 COMMENT '是否标星',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_user_subject (user_id, subject),
    INDEX idx_knowledge_point_id (knowledge_point_id)
) ENGINE=InnoDB COMMENT='用户个人笔记';

-- 9. 内容存档表
CREATE TABLE cpa_archive (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '存档ID',
    push_id         BIGINT NOT NULL COMMENT '关联推送ID',
    subject         TINYINT NOT NULL COMMENT '科目',
    push_date       DATE NOT NULL COMMENT '推送日期',
    knowledge_point_id BIGINT NOT NULL COMMENT '考点ID',
    file_path       VARCHAR(500) NOT NULL COMMENT '存档文件路径',
    file_format     VARCHAR(20) DEFAULT 'md' COMMENT '文件格式',
    file_size       BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_subject_date (subject, push_date),
    INDEX idx_push_id (push_id)
) ENGINE=InnoDB COMMENT='内容存档';

-- 10. 微信推送配置表
CREATE TABLE cpa_wechat_config (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    channel         TINYINT NOT NULL COMMENT '渠道：1公众号 2企业微信',
    openid          VARCHAR(100) COMMENT '微信openid',
    webhook_url     VARCHAR(500) COMMENT '企业微信webhook地址',
    template_id     VARCHAR(100) COMMENT '消息模板ID',
    push_time       VARCHAR(10) DEFAULT '07:30' COMMENT '推送时间 HH:mm',
    enabled         BIT(1) DEFAULT 1 COMMENT '是否启用',
    creator         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         BIT(1) DEFAULT 0 COMMENT '是否删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='微信推送配置';
