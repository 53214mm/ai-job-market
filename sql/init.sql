-- =============================================
-- AI 智能招聘市场 - 数据库初始化脚本
-- Database: ai_job_market
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 用户相关 (3张) ====================

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '雪花ID',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `password_hash` VARCHAR(256) NOT NULL COMMENT 'BCrypt密码哈希',
    `role` VARCHAR(16) NOT NULL DEFAULT 'SEEKER' COMMENT 'SEEKER/RECRUITER/ADMIN',
    `nickname` VARCHAR(64) COMMENT '昵称',
    `avatar_url` VARCHAR(512) COMMENT '头像URL',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户主表';

CREATE TABLE IF NOT EXISTS `user_seeker_profile` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '关联user',
    `real_name` VARCHAR(32) COMMENT '真实姓名',
    `gender` VARCHAR(8) COMMENT '性别',
    `birth_date` DATE COMMENT '出生日期',
    `education_level` VARCHAR(32) COMMENT '最高学历',
    `current_city` VARCHAR(64) COMMENT '现居城市',
    `expected_city` VARCHAR(64) COMMENT '期望城市',
    `expected_salary_min` INT COMMENT '期望最低薪资(K)',
    `expected_salary_max` INT COMMENT '期望最高薪资(K)',
    `job_status` VARCHAR(32) COMMENT '在职/离职/应届',
    `personal_summary` TEXT COMMENT '个人简介',
    `resume_count` INT DEFAULT 0 COMMENT '简历数量',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='求职者扩展信息';

CREATE TABLE IF NOT EXISTS `user_recruiter_profile` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '关联user',
    `real_name` VARCHAR(32) COMMENT '真实姓名',
    `position` VARCHAR(64) COMMENT '职位(如HR经理)',
    `company_id` BIGINT COMMENT '所属公司',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `verified` TINYINT(1) DEFAULT 0 COMMENT '是否认证',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招聘方扩展信息';

-- ==================== 公司相关 (2张) ====================

CREATE TABLE IF NOT EXISTS `company` (
    `id` BIGINT NOT NULL,
    `name` VARCHAR(128) NOT NULL COMMENT '公司全称',
    `short_name` VARCHAR(64) COMMENT '简称',
    `logo_url` VARCHAR(512) COMMENT 'Logo URL',
    `industry` VARCHAR(64) COMMENT '行业',
    `scale` VARCHAR(32) COMMENT '规模: 1-20/20-99/100-499/500-999/1000+',
    `stage` VARCHAR(32) COMMENT '融资阶段: 未融资/天使轮/A轮/B轮/C轮/上市',
    `website` VARCHAR(256) COMMENT '官网',
    `address` VARCHAR(256) COMMENT '地址',
    `description` TEXT COMMENT '公司介绍',
    `culture` TEXT COMMENT '企业文化',
    `welfare` TEXT COMMENT '福利待遇(JSON)',
    `verified` TINYINT(1) DEFAULT 0 COMMENT '是否认证',
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/DISABLED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_industry` (`industry`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司表';

CREATE TABLE IF NOT EXISTS `company_review` (
    `id` BIGINT NOT NULL,
    `company_id` BIGINT NOT NULL COMMENT '关联company',
    `user_id` BIGINT NOT NULL COMMENT '评价人',
    `rating` TINYINT NOT NULL COMMENT '评分1-5',
    `title` VARCHAR(256) COMMENT '评价标题',
    `content` TEXT COMMENT '评价内容',
    `pros` TEXT COMMENT '优点',
    `cons` TEXT COMMENT '缺点',
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司评价';

-- ==================== 简历相关 (6张) ====================

CREATE TABLE IF NOT EXISTS `resume` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '所属用户',
    `title` VARCHAR(128) COMMENT '简历名称',
    `full_name` VARCHAR(32) COMMENT '姓名',
    `email` VARCHAR(128) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `current_city` VARCHAR(64) COMMENT '现居城市',
    `expected_city` VARCHAR(64) COMMENT '期望城市',
    `expected_salary_min` INT COMMENT '期望最低薪资(K)',
    `expected_salary_max` INT COMMENT '期望最高薪资(K)',
    `job_status` VARCHAR(32) COMMENT '在职/离职/应届',
    `summary` TEXT COMMENT '自我评价',
    `privacy` VARCHAR(16) NOT NULL DEFAULT 'PUBLIC' COMMENT 'PUBLIC/APPLICATION_ONLY/PRIVATE',
    `is_default` TINYINT(1) DEFAULT 0 COMMENT '默认简历',
    `ai_score` INT COMMENT 'AI评分0-100',
    `ai_suggestion` TEXT COMMENT 'AI优化建议',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_privacy` (`privacy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历主表';

CREATE TABLE IF NOT EXISTS `resume_education` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `school_name` VARCHAR(128) NOT NULL COMMENT '学校名称',
    `degree` VARCHAR(32) COMMENT '学历: 高中/大专/本科/硕士/博士',
    `major` VARCHAR(128) COMMENT '专业',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `description` TEXT COMMENT '在校经历',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教育经历';

CREATE TABLE IF NOT EXISTS `resume_work_experience` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `company_name` VARCHAR(128) NOT NULL COMMENT '公司名称',
    `position` VARCHAR(128) COMMENT '职位',
    `industry` VARCHAR(64) COMMENT '行业',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `description` TEXT COMMENT '工作内容',
    `achievement` TEXT COMMENT '业绩成果',
    `skills_used` VARCHAR(512) COMMENT '使用的技能(逗号分隔)',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作经历';

CREATE TABLE IF NOT EXISTS `resume_project` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `project_name` VARCHAR(128) NOT NULL COMMENT '项目名称',
    `role` VARCHAR(64) COMMENT '担任角色',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `description` TEXT COMMENT '项目描述',
    `technologies` VARCHAR(512) COMMENT '技术栈',
    `achievement` TEXT COMMENT '项目成果',
    `project_url` VARCHAR(512) COMMENT '项目链接',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目经历';

CREATE TABLE IF NOT EXISTS `resume_skill` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `skill_name` VARCHAR(64) NOT NULL COMMENT '技能名称',
    `proficiency` VARCHAR(16) COMMENT '初学/熟练/精通/专家',
    `months_of_use` INT COMMENT '使用月数',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技能标签';

CREATE TABLE IF NOT EXISTS `resume_certificate` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `cert_name` VARCHAR(128) NOT NULL COMMENT '证书名称',
    `issuing_org` VARCHAR(128) COMMENT '颁发机构',
    `obtain_date` DATE COMMENT '获取日期',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证书';

-- ==================== 职位相关 (3张) ====================

CREATE TABLE IF NOT EXISTS `job` (
    `id` BIGINT NOT NULL,
    `company_id` BIGINT NOT NULL COMMENT '发布公司',
    `recruiter_id` BIGINT NOT NULL COMMENT '发布人',
    `title` VARCHAR(128) NOT NULL COMMENT '职位名称',
    `category` VARCHAR(64) COMMENT '职位类别',
    `experience_level` VARCHAR(32) COMMENT '经验要求: 应届/1-3年/3-5年/5-10年/不限',
    `education_level` VARCHAR(32) COMMENT '学历要求',
    `salary_min` INT COMMENT '月薪下限(K)',
    `salary_max` INT COMMENT '月薪上限(K)',
    `salary_months` INT DEFAULT 12 COMMENT '月数(12-18薪)',
    `city` VARCHAR(64) COMMENT '工作城市',
    `district` VARCHAR(64) COMMENT '区域',
    `address` VARCHAR(256) COMMENT '详细地址',
    `job_type` VARCHAR(32) COMMENT '全职/兼职/实习/远程',
    `head_count` INT DEFAULT 1 COMMENT '招聘人数',
    `description` TEXT COMMENT '职位描述',
    `requirement` TEXT COMMENT '任职要求',
    `welfare` TEXT COMMENT '福利待遇',
    `tags` VARCHAR(512) COMMENT '标签(逗号分隔)',
    `skills_required` VARCHAR(1024) COMMENT '所需技能',
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/CLOSED',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `apply_count` INT DEFAULT 0 COMMENT '投递量',
    `published_at` DATETIME COMMENT '发布时间',
    `expires_at` DATETIME COMMENT '过期时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_recruiter_id` (`recruiter_id`),
    KEY `idx_category` (`category`),
    KEY `idx_city` (`city`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位表';

CREATE TABLE IF NOT EXISTS `job_skill_tag` (
    `id` BIGINT NOT NULL,
    `job_id` BIGINT NOT NULL COMMENT '关联job',
    `skill_name` VARCHAR(64) NOT NULL COMMENT '技能名称',
    `is_required` TINYINT(1) DEFAULT 1 COMMENT '必须/加分项',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位技能标签';

CREATE TABLE IF NOT EXISTS `job_category_dict` (
    `id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID(0为顶级)',
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `level` INT DEFAULT 1 COMMENT '层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位分类字典';

-- ==================== 投递相关 (4张) ====================

CREATE TABLE IF NOT EXISTS `application` (
    `id` BIGINT NOT NULL,
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `resume_id` BIGINT NOT NULL COMMENT '简历ID',
    `seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `recruiter_id` BIGINT COMMENT '招聘方ID',
    `company_id` BIGINT NOT NULL COMMENT '公司ID',
    `status` VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT 'APPLIED/VIEWED/SCREENING/INTERVIEW/OFFER/HIRED/REJECTED/WITHDRAWN',
    `cover_letter` TEXT COMMENT '求职信',
    `ai_match_score` INT COMMENT 'AI匹配度0-100',
    `ai_match_reason` TEXT COMMENT 'AI匹配理由',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_seeker_id` (`seeker_id`),
    KEY `idx_recruiter_id` (`recruiter_id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投递记录';

CREATE TABLE IF NOT EXISTS `application_log` (
    `id` BIGINT NOT NULL,
    `application_id` BIGINT NOT NULL COMMENT '关联application',
    `from_status` VARCHAR(32) COMMENT '变更前状态',
    `to_status` VARCHAR(32) NOT NULL COMMENT '变更后状态',
    `operator_id` BIGINT COMMENT '操作人ID',
    `remark` VARCHAR(512) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投递状态日志';

CREATE TABLE IF NOT EXISTS `interview` (
    `id` BIGINT NOT NULL,
    `application_id` BIGINT NOT NULL COMMENT '关联application',
    `interview_type` VARCHAR(32) NOT NULL COMMENT 'PHONE/VIDEO/ONSITE/AI_MOCK',
    `scheduled_time` DATETIME COMMENT '面试时间',
    `duration_minutes` INT COMMENT '时长(分钟)',
    `location` VARCHAR(256) COMMENT '地点或视频链接',
    `interviewer` VARCHAR(64) COMMENT '面试官',
    `contact_phone` VARCHAR(20) COMMENT '联系电话',
    `status` VARCHAR(32) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED/COMPLETED/CANCELLED',
    `feedback` TEXT COMMENT '面试反馈',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_application_id` (`application_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试安排';

CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` VARCHAR(32) NOT NULL COMMENT 'JOB/RESUME',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏';

-- ==================== AI相关 (5张) ====================

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_type` VARCHAR(32) COMMENT 'CAREER_ADVICE/RESUME_HELP/INTERVIEW_PRACTICE/GENERAL',
    `title` VARCHAR(256) COMMENT '会话标题',
    `message_count` INT DEFAULT 0 COMMENT '消息数',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id` BIGINT NOT NULL,
    `session_id` BIGINT NOT NULL COMMENT '关联session',
    `role` VARCHAR(16) NOT NULL COMMENT 'USER/ASSISTANT/SYSTEM',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `tokens_used` INT COMMENT 'Token消耗',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息';

CREATE TABLE IF NOT EXISTS `ai_resume_analysis` (
    `id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL COMMENT '关联resume',
    `seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `overall_score` INT COMMENT '综合评分',
    `format_score` INT COMMENT '格式评分',
    `content_score` INT COMMENT '内容评分',
    `keyword_score` INT COMMENT '关键词匹配评分',
    `strengths` TEXT COMMENT '优势分析',
    `weaknesses` TEXT COMMENT '不足之处',
    `suggestions` TEXT COMMENT '优化建议',
    `optimized_content` TEXT COMMENT 'AI优化后的内容(JSON)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resume_id` (`resume_id`),
    KEY `idx_seeker_id` (`seeker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI简历分析';

CREATE TABLE IF NOT EXISTS `ai_job_match` (
    `id` BIGINT NOT NULL,
    `seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
    `resume_id` BIGINT NOT NULL COMMENT '简历ID',
    `job_id` BIGINT NOT NULL COMMENT '职位ID',
    `match_score` DECIMAL(5,2) COMMENT '匹配分0-100',
    `dimension_scores` JSON COMMENT '各维度分数(技能/经验/学历/薪资/地点)',
    `match_reason` TEXT COMMENT '匹配理由',
    `vector_similarity` DECIMAL(8,6) COMMENT '向量余弦相似度',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_seeker_id` (`seeker_id`),
    KEY `idx_job_id` (`job_id`),
    KEY `idx_match_score` (`match_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI职位匹配';

CREATE TABLE IF NOT EXISTS `ai_interview_record` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `job_id` BIGINT COMMENT '目标职位ID',
    `session_id` BIGINT COMMENT '关联ai_chat_session',
    `question` TEXT COMMENT 'AI提出的问题',
    `expected_answer_keywords` TEXT COMMENT '期望答案关键词',
    `user_answer` TEXT COMMENT '用户回答',
    `ai_feedback` TEXT COMMENT 'AI评估反馈',
    `score` INT COMMENT '回答评分',
    `question_index` INT COMMENT '题号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模拟面试记录';

-- ==================== 消息通知 (2张) ====================

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL COMMENT '接收人',
    `type` VARCHAR(32) NOT NULL COMMENT 'APPLICATION_UPDATE/INTERVIEW_INVITE/MESSAGE/SYSTEM',
    `title` VARCHAR(256) COMMENT '通知标题',
    `content` TEXT COMMENT '通知内容',
    `related_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id_read` (`user_id`, `is_read`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统通知';

CREATE TABLE IF NOT EXISTS `message` (
    `id` BIGINT NOT NULL,
    `sender_id` BIGINT NOT NULL COMMENT '发送方',
    `receiver_id` BIGINT NOT NULL COMMENT '接收方',
    `application_id` BIGINT COMMENT '关联投递(可选)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内私信';

-- ==================== 内容管理 + 系统 (3张) ====================

CREATE TABLE IF NOT EXISTS `article` (
    `id` BIGINT NOT NULL,
    `category_id` BIGINT COMMENT '分类ID',
    `author_id` BIGINT COMMENT '作者ID',
    `title` VARCHAR(256) NOT NULL COMMENT '文章标题',
    `summary` VARCHAR(512) COMMENT '摘要',
    `content` LONGTEXT COMMENT '文章内容',
    `cover_url` VARCHAR(512) COMMENT '封面图',
    `tags` VARCHAR(256) COMMENT '标签',
    `view_count` INT DEFAULT 0 COMMENT '阅读量',
    `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED',
    `published_at` DATETIME COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_published_at` (`published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章';

CREATE TABLE IF NOT EXISTS `article_category` (
    `id` BIGINT NOT NULL,
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类';

CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id` BIGINT NOT NULL,
    `dict_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
    `dict_key` VARCHAR(64) NOT NULL COMMENT '字典键',
    `dict_value` VARCHAR(256) NOT NULL COMMENT '字典值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(256) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统字典';

-- ==================== 初始化数据 ====================

-- 职位分类
INSERT INTO `job_category_dict` (`id`, `parent_id`, `name`, `level`, `sort_order`) VALUES
(1, 0, '技术', 1, 1),
(2, 0, '产品', 1, 2),
(3, 0, '设计', 1, 3),
(4, 0, '运营', 1, 4),
(5, 0, '市场', 1, 5),
(6, 0, '销售', 1, 6),
(7, 0, '职能', 1, 7),
(8, 0, '金融', 1, 8),
(9, 0, '教育', 1, 9),
(10, 0, '医疗', 1, 10);

-- 文章分类
INSERT INTO `article_category` (`id`, `name`, `sort_order`) VALUES
(1, '求职攻略', 1),
(2, '行业资讯', 2),
(3, '面试经验', 3),
(4, '职场技能', 4);

SET FOREIGN_KEY_CHECKS = 1;
