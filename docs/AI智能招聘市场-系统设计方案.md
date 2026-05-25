# AI 智能招聘市场 —— 系统设计方案

> 技术栈: Spring Boot 3.5.2 + MyBatis-Plus 3.5.9 + Spring AI 1.1.2 + DashScope(Qwen-plus) + PostgreSQL(pgvector) + Redis + Thymeleaf + Knife4j

---

## 目录

- [一、系统模块划分](#一系统模块划分)
- [二、数据库表设计](#二数据库表设计)
- [三、页面结构](#三页面结构)
- [四、API 接口设计](#四api-接口设计)
- [五、AI 功能设计](#五ai-功能设计)
- [六、实施策略](#六实施策略)
- [七、已有基础设施](#七已有基础设施)

---

## 一、系统模块划分

```
┌─────────────────────────────────────────────────────┐
│                  AI 智能招聘市场                       │
├──────────┬──────────┬──────────┬────────────────────┤
│ 用户模块  │ 简历模块  │ 公司模块  │     职位模块        │
├──────────┼──────────┼──────────┼────────────────────┤
│ 投递模块  │ AI智能   │ 消息通知  │   内容管理模块       │
└──────────┴──────────┴──────────┴────────────────────┘
```

### 1. 用户模块
- 注册/登录（邮箱+密码、手机验证码）
- 三角色：**求职者 (SEEKER)** / **招聘方 (RECRUITER)** / **管理员 (ADMIN)**
- 个人信息管理、密码修改、头像上传
- 企业认证申请（招聘方关联公司）

### 2. 简历模块
- 在线创建/编辑简历（教育经历、工作经历、项目经历、技能标签、证书）
- **AI 简历分析**：自动评分、问题诊断、优化建议
- **AI 简历优化**：根据目标岗位自动润色简历内容
- 简历隐私：公开 / 仅投递可见 / 私密
- 支持多份简历，可设默认简历
- PDF 简历导出（已有 iText 基础设施）

### 3. 公司模块
- 公司注册与信息维护
- 企业认证（营业执照上传 + 审核）
- 公司主页（介绍、文化、福利、在招职位）
- 公司评价/评分体系

### 4. 职位模块
- 职位 CRUD（招聘方发布/编辑/下架）
- 多维度分类：行业类别 + 经验要求 + 学历 + 工作类型
- 标签系统：远程、实习、校招、社招、急聘等
- **AI 智能推荐**：基于简历向量与职位向量的语义匹配
- **向量语义搜索**：pgvector 支持"用简历内容搜职位"
- 职位浏览量、投递量统计

### 5. 投递模块
- 选择简历一键投递
- 投递状态全流程追踪：
  ```
  APPLIED → VIEWED → SCREENING → INTERVIEW → OFFER → HIRED
     ↓         ↓                                  ↓
  WITHDRAWN  REJECTED                          REJECTED
  ```
- 面试安排（电话/视频/线下/AI模拟）
- 收藏职位 / 收藏简历（双向）
- 投递历史与状态变更日志

### 6. AI 智能模块
- **AI 求职问答**：基于 RAG 的智能求职咨询（已有 Agent + RAG + 聊天记忆）
- **AI 职位匹配**：简历 ↔ 职位向量余弦相似度计算 + 多维评分
- **AI 模拟面试**：根据岗位 JD 自动生成面试题，评估回答质量
- **AI 简历分析**：格式、内容、关键词多维度评分 + 润色建议
- **AI 招聘助手**：辅助 HR 筛选简历、生成面试问题、候选人排序

### 7. 消息通知模块
- 系统通知：投递状态变更、面试邀请、offer 通知
- 站内私信：求职者 ↔ HR 实时沟通
- 邮件通知（可选）

### 8. 内容管理模块
- 求职攻略 / 行业资讯 / 面试经验 / 职场技能 文章
- FAQ 常见问题
- 后台 CMS 管理

---

## 二、数据库表设计

> 总计 **28 张表**，主键统一使用雪花算法（MyBatis-Plus IdType.ASSIGN_ID）

### 2.1 用户相关 (3 张)

#### user（用户主表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 雪花ID |
| email | VARCHAR(128) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| password_hash | VARCHAR(256) | BCrypt 密码哈希 |
| role | VARCHAR(16) | SEEKER / RECRUITER / ADMIN |
| nickname | VARCHAR(64) | 昵称 |
| avatar_url | VARCHAR(512) | 头像URL |
| status | VARCHAR(16) | ACTIVE / DISABLED |
| last_login_time | TIMESTAMP | 最后登录 |
| created_at / updated_at | TIMESTAMP | 创建/更新时间 |

#### user_seeker_profile（求职者扩展信息）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT UNIQUE | 关联 user |
| real_name | VARCHAR(32) | 真实姓名 |
| gender | VARCHAR(8) | 性别 |
| birth_date | DATE | 出生日期 |
| education_level | VARCHAR(32) | 最高学历 |
| current_city / expected_city | VARCHAR(64) | 现居/期望城市 |
| expected_salary_min / max | INT | 期望薪资范围(K) |
| job_status | VARCHAR(32) | 在职/离职/应届 |
| personal_summary | TEXT | 个人简介 |

#### user_recruiter_profile（招聘方扩展信息）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT UNIQUE | 关联 user |
| real_name | VARCHAR(32) | 真实姓名 |
| position | VARCHAR(64) | 职位(如HR经理) |
| company_id | BIGINT | 所属公司 |
| phone | VARCHAR(20) | 联系电话 |
| verified | BOOLEAN | 是否认证 |

---

### 2.2 公司相关 (2 张)

#### company（公司表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| name | VARCHAR(128) | 公司全称 |
| short_name | VARCHAR(64) | 简称 |
| logo_url | VARCHAR(512) | Logo |
| industry | VARCHAR(64) | 行业(互联网/金融/教育...) |
| scale | VARCHAR(32) | 规模(1-20/20-99/100-499/500-999/1000+) |
| stage | VARCHAR(32) | 融资阶段(未融资/天使轮/A轮/B轮/C轮/上市) |
| website | VARCHAR(256) | 官网 |
| address | VARCHAR(256) | 地址 |
| description | TEXT | 公司介绍 |
| welfare | TEXT | 福利(JSON数组) |
| verified | BOOLEAN | 认证状态 |
| status | VARCHAR(16) | ACTIVE / DISABLED |

#### company_review（公司评价）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| company_id | BIGINT | 关联 company |
| user_id | BIGINT | 评价人 |
| rating | INT | 评分 1-5 |
| title / content | VARCHAR/TEXT | 评价内容 |
| pros / cons | TEXT | 优点/缺点 |
| status | VARCHAR(16) | PENDING / APPROVED / REJECTED |

---

### 2.3 简历相关 (6 张)

#### resume（简历主表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT | 所属用户 |
| title | VARCHAR(128) | 简历名称 |
| full_name / email / phone | - | 基本信息 |
| current_city / expected_city | VARCHAR(64) | 现居/期望城市 |
| expected_salary_min / max | INT | 期望薪资 |
| summary | TEXT | 自我评价 |
| privacy | VARCHAR(16) | PUBLIC / APPLICATION_ONLY / PRIVATE |
| is_default | BOOLEAN | 默认简历 |
| **ai_score** | INT | **AI 综合评分 0-100** |
| **ai_suggestion** | TEXT | **AI 优化建议** |

#### resume_education（教育经历）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| resume_id | BIGINT FK | |
| school_name | VARCHAR(128) | 学校名称 |
| degree | VARCHAR(32) | 学历 |
| major | VARCHAR(128) | 专业 |
| start_date / end_date | DATE | 起止时间 |
| description | TEXT | 在校经历 |

#### resume_work_experience（工作经历）
| 字段 | 类型 | 说明 |
|------|------|------|
| company_name | VARCHAR(128) | 公司名称 |
| position | VARCHAR(128) | 职位 |
| industry | VARCHAR(64) | 行业 |
| description | TEXT | 工作内容 |
| achievement | TEXT | 业绩成果 |
| skills_used | VARCHAR(512) | 使用的技能 |

#### resume_project（项目经历）
| 字段 | 类型 | 说明 |
|------|------|------|
| project_name | VARCHAR(128) | 项目名称 |
| role | VARCHAR(64) | 担任角色 |
| description | TEXT | 项目描述 |
| technologies | VARCHAR(512) | 技术栈 |
| achievement | TEXT | 项目成果 |

#### resume_skill（技能标签）
| 字段 | 类型 | 说明 |
|------|------|------|
| skill_name | VARCHAR(64) | 技能名称 |
| proficiency | VARCHAR(16) | 初学/熟练/精通/专家 |
| months_of_use | INT | 使用月数 |

#### resume_certificate（证书）
| 字段 | 类型 | 说明 |
|------|------|------|
| cert_name | VARCHAR(128) | 证书名称 |
| issuing_org | VARCHAR(128) | 颁发机构 |
| obtain_date | DATE | 获取日期 |

---

### 2.4 职位相关 (3 张)

#### job（职位表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| company_id | BIGINT FK | 发布公司 |
| recruiter_id | BIGINT FK | 发布人 |
| title | VARCHAR(128) | 职位名称 |
| category | VARCHAR(64) | 类别(技术/产品/设计/运营...) |
| experience_level | VARCHAR(32) | 应届/1-3年/3-5年/5-10年/不限 |
| education_level | VARCHAR(32) | 学历要求 |
| salary_min / max | INT | 月薪范围(K) |
| salary_months | INT | 月数(12-18薪) |
| city / district | VARCHAR(64) | 工作城市/区域 |
| job_type | VARCHAR(32) | 全职/兼职/实习/远程 |
| description | TEXT | 职位描述 |
| requirement | TEXT | 任职要求 |
| skills_required | VARCHAR(1024) | 所需技能 |
| status | VARCHAR(16) | DRAFT / PUBLISHED / CLOSED |
| view_count / apply_count | INT | 浏览量/投递量 |
| **vector_embedding** | **vector(1536)** | **pgvector 语义搜索向量** |
| published_at / expires_at | TIMESTAMP | 发布时间/过期时间 |

#### job_skill_tag（职位技能标签）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| job_id | BIGINT FK | |
| skill_name | VARCHAR(64) | 技能名 |
| is_required | BOOLEAN | 必须/加分项 |

#### job_category_dict（职位分类字典）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| parent_id | BIGINT | 父分类(0为顶级) |
| name | VARCHAR(64) | 分类名 |
| level / sort_order | INT | 层级/排序 |

---

### 2.5 投递相关 (4 张)

#### application（投递记录）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| job_id / resume_id | BIGINT FK | 职位/简历 |
| seeker_id / recruiter_id | BIGINT FK | 求职者/招聘方 |
| company_id | BIGINT FK | 公司 |
| status | VARCHAR(32) | APPLIED→VIEWED→SCREENING→INTERVIEW→OFFER→HIRED/REJECTED |
| cover_letter | TEXT | 求职信 |
| **ai_match_score** | INT | **AI 匹配度 0-100** |
| **ai_match_reason** | TEXT | **AI 匹配理由** |

#### application_log（投递状态日志）
| 字段 | 类型 | 说明 |
|------|------|------|
| application_id | BIGINT FK | |
| from_status / to_status | VARCHAR(32) | 状态变更 |
| operator_id | BIGINT | 操作人 |
| remark | VARCHAR(512) | 备注 |

#### interview（面试安排）
| 字段 | 类型 | 说明 |
|------|------|------|
| application_id | BIGINT FK | |
| interview_type | VARCHAR(32) | PHONE / VIDEO / ONSITE / AI_MOCK |
| scheduled_time | TIMESTAMP | 面试时间 |
| location | VARCHAR(256) | 地点或视频链接 |
| status | VARCHAR(32) | SCHEDULED / COMPLETED / CANCELLED |
| feedback | TEXT | 面试反馈 |

#### favorite（收藏）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT | |
| target_type | VARCHAR(32) | JOB / RESUME |
| target_id | BIGINT | 目标ID |
| UNIQUE(user_id, target_type, target_id) | | 防止重复收藏 |

---

### 2.6 AI 相关 (5 张)

#### ai_chat_session（AI 对话会话）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| user_id | BIGINT | |
| session_type | VARCHAR(32) | CAREER_ADVICE / RESUME_HELP / INTERVIEW_PRACTICE / GENERAL |
| title | VARCHAR(256) | 会话标题 |
| message_count | INT | 消息数 |

#### ai_chat_message（AI 对话消息）
| 字段 | 类型 | 说明 |
|------|------|------|
| session_id | BIGINT FK | |
| role | VARCHAR(16) | USER / ASSISTANT / SYSTEM |
| content | TEXT | 消息内容 |
| tokens_used | INT | Token 消耗 |

#### ai_resume_analysis（AI 简历分析）
| 字段 | 类型 | 说明 |
|------|------|------|
| resume_id | BIGINT FK | |
| overall_score | INT | 综合评分 |
| format_score | INT | 格式评分 |
| content_score | INT | 内容评分 |
| keyword_score | INT | 关键词评分 |
| strengths / weaknesses | TEXT | 优势/不足 |
| suggestions | TEXT | 优化建议 |
| optimized_content | TEXT | AI优化结果(JSON) |

#### ai_job_match（AI 职位匹配）
| 字段 | 类型 | 说明 |
|------|------|------|
| seeker_id / resume_id / job_id | BIGINT FK | |
| match_score | DECIMAL(5,2) | 综合匹配度 0-100 |
| dimension_scores | JSONB | 各维度分数(技能/经验/学历/薪资/地点) |
| vector_similarity | DECIMAL(8,6) | 向量余弦相似度 |

#### ai_interview_record（AI 模拟面试记录）
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id / job_id / session_id | BIGINT FK | |
| question / user_answer | TEXT | 问题/回答 |
| expected_answer_keywords | TEXT | 期望关键词 |
| ai_feedback | TEXT | AI 评估反馈 |
| score | INT | 回答评分 |
| question_index | INT | 题号 |

---

### 2.7 消息通知 (2 张)

#### notification（系统通知）
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 接收人 |
| type | VARCHAR(32) | APPLICATION_UPDATE / INTERVIEW_INVITE / MESSAGE / SYSTEM |
| title / content | VARCHAR/TEXT | 通知内容 |
| related_id | BIGINT | 关联业务ID |
| is_read | BOOLEAN | 已读 |

#### message（站内私信）
| 字段 | 类型 | 说明 |
|------|------|------|
| sender_id / receiver_id | BIGINT | 发送方/接收方 |
| application_id | BIGINT | 关联投递(可选) |
| content | TEXT | 消息内容 |
| is_read | BOOLEAN | 已读 |

---

### 2.8 内容管理 + 系统 (3 张)

#### article（文章）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | |
| category_id | BIGINT FK | 分类 |
| author_id | BIGINT FK | 作者 |
| title / summary / content | - | 标题/摘要/正文 |
| tags | VARCHAR(256) | 标签 |
| view_count | INT | 阅读量 |
| status | VARCHAR(16) | DRAFT / PUBLISHED |

#### article_category（文章分类）
| 字段 | 类型 | 说明 |
|------|------|------|
| name | VARCHAR(64) | 求职攻略/行业资讯/面试经验/职场技能 |
| sort_order | INT | 排序 |

#### sys_dict（系统字典）
| 字段 | 类型 | 说明 |
|------|------|------|
| dict_type | VARCHAR(64) | 字典类型 |
| dict_key | VARCHAR(64) | 键 |
| dict_value | VARCHAR(256) | 值 |

---

## 三、页面结构

### 3.1 前台（求职者/游客）

| 页面 | 路径 | 功能 |
|------|------|------|
| 首页 | `/` | 搜索框 + 热门职位 + AI推荐职位 + 热门公司 |
| 职位搜索 | `/jobs` | 关键词搜索 + 条件筛选 + AI语义搜索 + 分页 |
| 职位详情 | `/jobs/{id}` | 职位详情 + 公司信息 + AI匹配度 + 投递 |
| 公司列表 | `/companies` | 公司搜索/浏览/分页 |
| 公司详情 | `/companies/{id}` | 公司主页 + 在招职位 + 评价 |
| 登录 | `/login` | 登录表单 |
| 注册 | `/register` | 注册表单(选择角色) |
| 个人中心 | `/profile` | 个人信息编辑 |
| 我的简历 | `/resumes` | 简历列表管理 |
| 简历编辑 | `/resumes/{id}/edit` | 分步表单(基本信息→教育→工作→项目→技能) |
| AI简历分析 | `/resumes/{id}/analysis` | AI评分展示 + 优化建议 + 一键优化 |
| 我的投递 | `/applications` | 投递记录 + 状态筛选 |
| 面试安排 | `/interviews` | 面试日程日历视图 |
| 我的收藏 | `/favorites` | 收藏职位列表 |
| **AI求职助手** | `/ai/chat` | AI对话咨询(复用已有聊天框架) |
| **AI模拟面试** | `/ai/interview` | 选择目标职位 → AI出题 → 回答 → AI评分 |
| 消息中心 | `/messages` | 通知列表 + 私信对话 |
| 求职攻略 | `/articles` | 文章列表 |
| 文章详情 | `/articles/{id}` | 文章内容 |

### 3.2 后台（招聘方）

| 页面 | 路径 | 功能 |
|------|------|------|
| 招聘看板 | `/recruiter` | 数据概览(发布职位数/收到简历数/面试数) |
| 职位管理 | `/recruiter/jobs` | 我发布的职位列表 |
| 发布职位 | `/recruiter/jobs/create` | 职位表单(描述+要求+技能标签) |
| 简历筛选 | `/recruiter/applications` | 收到的投递 + AI匹配排序 + 筛选 |
| 候选人详情 | `/recruiter/applications/{id}` | 简历查看 + AI分析 + 状态操作 |
| **AI 招聘助手** | `/recruiter/ai` | AI辅助筛选 + 生成面试问题 |
| 面试管理 | `/recruiter/interviews` | 面试安排与反馈 |

### 3.3 管理后台（管理员）

| 页面 | 路径 | 功能 |
|------|------|------|
| 仪表盘 | `/admin` | 平台数据统计 |
| 用户管理 | `/admin/users` | 用户列表/禁用/角色管理 |
| 公司审核 | `/admin/companies` | 企业认证审核 |
| 职位管理 | `/admin/jobs` | 职位审核/下架 |
| 内容管理 | `/admin/articles` | 文章发布/编辑 |
| 字典管理 | `/admin/dict` | 系统字典维护 |

---

## 四、API 接口设计

### 4.1 用户模块 API
```
POST   /api/user/register          # 注册
POST   /api/user/login             # 登录(返回JWT token)
GET    /api/user/current           # 获取当前用户信息
PUT    /api/user/profile           # 更新个人信息
PUT    /api/user/password           # 修改密码
GET    /api/user/{id}              # 查看用户公开信息
```

### 4.2 简历模块 API
```
GET    /api/resumes                # 我的简历列表
POST   /api/resumes                # 创建简历
GET    /api/resumes/{id}           # 简历详情
PUT    /api/resumes/{id}           # 更新简历
DELETE /api/resumes/{id}           # 删除简历
POST   /api/resumes/{id}/ai-analyze   # AI 分析简历
POST   /api/resumes/{id}/ai-optimize  # AI 优化简历
POST   /api/resumes/{id}/export-pdf    # 导出PDF简历
```

### 4.3 职位模块 API
```
GET    /api/jobs                   # 职位列表(搜索+筛选+分页)
GET    /api/jobs/search/semantic   # AI 语义搜索
GET    /api/jobs/{id}              # 职位详情
POST   /api/jobs                   # 发布职位(招聘方)
PUT    /api/jobs/{id}              # 编辑职位
PUT    /api/jobs/{id}/close        # 关闭职位
GET    /api/jobs/recommend         # AI 推荐职位(需登录)
```

### 4.4 投递模块 API
```
POST   /api/applications           # 投递职位
GET    /api/applications/my        # 我的投递(求职者)
GET    /api/applications/received  # 收到的投递(招聘方)
GET    /api/applications/{id}      # 投递详情
PUT    /api/applications/{id}/status  # 更新状态
POST   /api/applications/{id}/interview  # 安排面试
POST   /api/favorites              # 添加收藏
DELETE /api/favorites/{id}         # 取消收藏
GET    /api/favorites              # 收藏列表
```

### 4.5 AI 模块 API（复用已有 AiController）
```
POST   /api/ai/chat                # AI 对话(同步)
GET    /api/ai/chat/stream         # AI 对话(SSE流式)
POST   /api/ai/chat/report         # AI 结构化输出
POST   /api/ai/chat/rag            # AI RAG 问答
POST   /api/ai/chat/tools          # AI 工具调用
POST   /api/ai/interview/start     # 开始AI模拟面试
POST   /api/ai/interview/answer    # 提交回答并获取反馈
```

---

## 五、AI 功能设计

### 5.1 AI 职位匹配（核心功能）

```
求职者简历 ──→ DashScope Embedding ──→ 简历向量
                                           │
                                    余弦相似度计算
                                           │
职位描述   ──→ DashScope Embedding ──→ 职位向量 ──→ ai_job_match
```

- 发布职位时自动生成 `vector_embedding`
- 用户查看职位列表时计算匹配度并排序
- 匹配维度：技能重合度、经验匹配、学历匹配、薪资匹配、地点匹配

### 5.2 AI 简历分析

利用现有 `JobApp.chatWithReport()` 结构化输出能力，定义 JSON Schema：
```json
{
  "overall_score": 85,
  "format_score": 90,
  "content_score": 80,
  "keyword_score": 85,
  "strengths": ["项目经验丰富", "技术栈匹配"],
  "weaknesses": ["缺少量化成果", "自我评价过于笼统"],
  "suggestions": ["建议在项目中增加数据指标", "..."],
  "optimized_summary": "优化后的自我评价..."
}
```

### 5.3 AI 求职对话助手

复用已有完整基础设施：
- **Agent**: `JobAgent` (ReAct + ToolCall)
- **RAG**: `JobAppRagCloudAdvisorConfig` + `JobAppDocumentLoader`
- **Memory**: `RedisBasedChatMemory` (对话持久化)
- **Tools**: 网页搜索、网页抓取、PDF生成、文件操作

### 5.4 AI 模拟面试

1. 用户选择目标职位
2. AI 读取职位 JD，生成 5-10 道面试题（技术+行为）
3. 用户逐题回答
4. AI 评估每道题的回答质量（关键词覆盖、逻辑性、完整性）
5. 生成面试报告

---

## 六、实施策略

### 第一阶段：核心闭环 MVP（2-3周）
**目标：跑通求职招聘基本流程**

| 任务 | 内容 |
|------|------|
| 建表 | 执行全部 DDL |
| 用户模块 | 注册/登录/JWT/角色管理 |
| 简历模块 | 简历 CRUD + 在线编辑 |
| 职位模块 | 职位发布 + 关键词搜索 + 分页 |
| 公司模块 | 公司注册 + 主页展示 |
| 投递模块 | 投递 + 状态流转 |
| 前端页面 | 首页 + 职位搜索 + 详情 + 投递流程 |

### 第二阶段：AI 赋能（2-3周）
**目标：AI 差异化功能上线**

| 任务 | 内容 |
|------|------|
| AI 简历分析 | 评分 + 建议 + 优化 |
| AI 职位匹配 | pgvector 向量化 + 语义搜索 + 匹配度排序 |
| AI 求职对话 | 对接已有 Agent/RAG/聊天记忆 |
| AI 模拟面试 | 出题 + 评估 + 报告 |
| AI 招聘助手 | 简历筛选辅助 |

### 第三阶段：体验与运营（1-2周）
**目标：完整产品体验**

| 任务 | 内容 |
|------|------|
| 消息通知 | 系统通知 + 站内私信 + 邮件 |
| 内容管理 | 求职攻略文章系统 |
| 公司评价 | 评分 + 评价审核 |
| 管理后台 | 数据统计 + 审核流程 |
| 性能优化 | Redis 缓存 + SQL 优化 |

---

## 七、已有基础设施

项目中已存在、可直接复用的组件：

| 组件 | 路径 | 用途 |
|------|------|------|
| JobAgent | `AI/agent/JobAgent.java` | ReAct + ToolCall AI Agent |
| JobApp | `AI/app/JobApp.java` | 统一 AI 调用入口(chat/stream/rag/tools) |
| RAG 管道 | `AI/rag/` | 文档加载 + 向量存储 + 查询增强 |
| Redis 聊天记忆 | `AI/chatMemory/RedisBasedChatMemory.java` | 对话持久化 |
| 文件聊天记忆 | `AI/chatMemory/FileBasedChatMemory.java` | 文件持久化备选 |
| 工具集 | `AI/tools/` | 搜索/抓取/下载/文件/终端/PDF/终止 |
| PDF 工具 | `AI/tools/PdfUtil.java` | 简历 PDF 生成(中文支持) |
| 全局异常处理 | `exception/GlobalExceptionHandler.java` | 统一错误响应 |
| 通用响应 | `common/BaseResponse.java` + `ResultUtils.java` | API 响应标准 |
| 分页请求 | `common/PageRequest.java` | 通用分页 DTO |
| 雪花ID | `config/JacksonConfig.java` | Long→String 序列化 |
| 分页插件 | `config/MyBatisPlusConfig.java` | MyBatis-Plus 分页 |
| CORS | `config/CorsConfig.java` | 跨域配置 |
| Knife4j | pom.xml 已引入 | API 文档 |

---

## 八、目录结构规划

```
src/main/java/com/li/ai_job_market/
├── AiJobMarketApplication.java
├── entity/                          # 实体类
│   ├── User.java
│   ├── UserSeekerProfile.java
│   ├── UserRecruiterProfile.java
│   ├── Company.java
│   ├── Resume.java
│   ├── ResumeEducation.java
│   ├── ResumeWorkExperience.java
│   ├── ResumeProject.java
│   ├── ResumeSkill.java
│   ├── Job.java
│   ├── Application.java
│   ├── Interview.java
│   ├── Favorite.java
│   ├── Notification.java
│   ├── Message.java
│   ├── Article.java
│   └── ...
├── mapper/                          # MyBatis-Plus Mapper
│   ├── UserMapper.java
│   ├── ResumeMapper.java
│   ├── JobMapper.java
│   ├── ApplicationMapper.java
│   └── ...
├── service/                         # 业务逻辑
│   ├── UserService.java
│   ├── ResumeService.java
│   ├── JobService.java
│   ├── ApplicationService.java
│   ├── AiMatchService.java          # AI 匹配服务
│   ├── AiResumeAnalysisService.java # AI 简历分析
│   ├── AiInterviewService.java      # AI 模拟面试
│   └── impl/
├── controller/                      # API 控制器
│   ├── UserController.java
│   ├── ResumeController.java
│   ├── JobController.java
│   ├── ApplicationController.java
│   ├── CompanyController.java
│   ├── NotificationController.java
│   └── ...
├── dto/                             # 数据传输对象
│   ├── request/
│   └── response/
├── config/                          # 已有配置类
├── common/                          # 已有通用类
├── exception/                       # 已有异常类
├── AI/                              # 已有AI基础设施
└── annotation/                      # 已有注解

src/main/resources/
├── application.yaml
├── application-local.yaml
├── mapper/                          # MyBatis XML(可选)
├── templates/                       # Thymeleaf 模板
│   ├── index.html                   # 首页
│   ├── jobs/
│   │   ├── list.html                # 职位列表
│   │   └── detail.html              # 职位详情
│   ├── resumes/
│   │   ├── list.html                # 简历列表
│   │   └── edit.html                # 简历编辑
│   ├── applications/
│   │   └── list.html                # 投递记录
│   ├── ai/
│   │   ├── chat.html                # AI 求职助手
│   │   └── interview.html           # AI 模拟面试
│   ├── recruiter/                   # 招聘方页面
│   ├── admin/                       # 管理后台页面
│   └── fragments/                   # 公共片段(header/footer)
└── static/
    ├── css/
    ├── js/
    └── images/
```
