# AI 智能招聘市场

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D)](https://vuejs.org/)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

基于 Spring Boot + Vue 3 的全栈智能招聘平台，集成 AI 大模型（通义千问 Qwen-Max）、RAG 知识增强检索、pgvector 向量语义搜索、WebSocket 实时通讯，覆盖职位发布、简历管理、AI 匹配、面试安排、站内私信的完整招聘闭环。

---

## 功能概览

| 角色 | 功能 |
|------|------|
| **求职者** | 职位搜索/浏览、AI 语义搜索、简历创建（含 PDF 上传解析）、投递职位、AI 简历分析优化、AI 面试模拟、AI 助手、收藏职位、站内私信 |
| **招聘方** | 发布/管理职位、查看/筛选简历、状态流转（已投递→面试→Offer）、安排面试、公司信息管理、AI 助手 |
| **管理员** | 用户管理（禁用/解禁）、职位管理（下架/上架）、内容管理（求职攻略/行业资讯）、企业审核 |

---

## 技术栈

### 后端

- **框架**：Spring Boot 3.5.2 + MyBatis-Plus 3.5.9
- **AI**：Spring AI 1.1.2 + 阿里云 DashScope（Qwen-Max）+ RAG（pgvector）
- **数据库**：MySQL 8.0 + PostgreSQL 16（pgvector 向量扩展）
- **缓存/会话**：Redis 7
- **消息**：STOMP over WebSocket（实时私信推送）
- **安全**：Spring Security + JWT 无状态认证
- **邮件**：Spring Mail + QQ SMTP（注册验证码）
- **文档**：Knife4j + SpringDoc OpenAPI
- **PDF**：iText Core 9.1

### 前端

- **框架**：Vue 3（Composition API）+ Vue Router 4
- **构建**：Vite 8
- **样式**：Tailwind CSS 4
- **WebSocket**：@stomp/stompjs

---

## 快速开始（本地开发）

### 环境要求

- JDK 21
- Maven 3.9+
- Node.js 22+
- MySQL 8.0+
- PostgreSQL 16 + pgvector 扩展
- Redis 7

### 1. 初始化数据库

```bash
# MySQL
mysql -u root -p < sql/init.sql
mysql -u root -p < sql/seed.sql

# PostgreSQL（启用 pgvector 扩展后）
psql -U root -d ai_job_market -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 2. 配置环境变量

编辑 `src/main/resources/application-local.yaml`，填入实际值：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_job_market
    username: root
    password: your-password
  data:
    redis:
      host: localhost
      password: your-redis-password
  mail:
    username: your-email@qq.com
    password: your-qq-auth-code
  ai:
    dashscope:
      api-key: sk-your-dashscope-key

pgvector:
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_job_market
    username: root
    password: your-pg-password

jwt:
  secret: your-jwt-secret-at-least-32-chars
```

### 3. 启动后端

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

后端运行在 `http://localhost:8123/api`，Swagger 文档在 `http://localhost:8123/api/swagger-ui.html`

### 4. 启动前端

```bash
cd ai-job-market-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，API 请求通过 Vite proxy 转发到后端。

### 5. 测试账号

| 角色 | 邮箱 | 密码 |
|------|------|------|
| 求职者 | seeker@test.com | 123456 |
| 招聘方 | recruiter@test.com | 123456 |
| 管理员 | admin@test.com | 123456 |

---

## 项目结构

```
ai_job_market/
├── src/main/java/com/li/ai_job_market/
│   ├── AI/              # AI 模块（智能体、RAG、工具、对话记忆）
│   │   ├── agent/       #   ReAct 智能体
│   │   ├── rag/         #   RAG 知识增强检索
│   │   ├── tools/       #   MCP 工具集
│   │   └── chatMemory/  #   对话记忆（Redis/File）
│   ├── config/          # Spring 配置（Security, WebSocket, CORS, JWT...）
│   ├── controller/      # REST 控制器
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis 数据访问层
│   ├── model/           # 数据模型（entity/dto/vo/enums）
│   └── utils/           # 工具类
├── ai-job-market-frontend/
│   └── src/
│       ├── components/  # 通用组件（JobCard, SearchBar...）
│       ├── composables/ # 组合式 API（useStomp.js）
│       └── pages/       # 页面组件（31 个）
├── sql/                 # 数据库脚本
├── docs/                # 项目文档（10 篇）
├── Dockerfile           # Docker 构建
├── docker-compose.yml   # 服务编排
├── nginx.conf           # Nginx 配置
└── .env.example         # 环境变量模板
```

---

## 核心功能详解

### AI 语义搜索

基于 pgvector 的职位向量相似度搜索：

```
职位发布 → 文本拼接 → DashScope Embedding API（1024维）→ JDBC 写入 job_vectors
搜索查询 → 向量化 → 余弦相似度检索 TOP-N → MySQL 反查完整数据
```

### 私信实时推送

STOMP over WebSocket，支持文字私信的实时发送/接收：

- 用户级路由：`/user/{userId}/queue/messages`
- JWT 握手 + STOMP CONNECT 双重认证
- 自动断线重连（5 秒间隔）
- 消息实时通知 + 角标更新

### AI 简历分析

AI 自动评分简历并给出改进建议：

- 综合评分 / 格式评分 / 内容评分 / 关键词匹配
- 优点与不足分析
- 针对性改进建议

### 邮箱验证码注册

- QQ 邮箱 SMTP 发送 6 位验证码
- 5 分钟有效期
- 注册前强制验证

---

## 文档

| 文档 | 说明 |
|------|------|
| [项目总览](docs/项目总览.md) | 完整项目全景介绍 |
| [系统设计方案](docs/AI智能招聘市场-系统设计方案.md) | 架构设计文档 |
| [AI 模块指南](docs/AI智能模块实现指南.md) | AI 功能详解 |
| [用户模块指南](docs/用户模块实现指南.md) | 用户注册/登录/JWT |
| [职位模块指南](docs/职位模块实现指南.md) | 职位 CRUD + 语义搜索 |
| [简历模块指南](docs/简历模块实现指南.md) | 简历管理 + AI 分析 |
| [投递模块指南](docs/投递模块实现指南.md) | 投递流程 + 状态管理 |
| [公司模块指南](docs/公司模块实现指南.md) | 公司管理 + 评价 |
| [消息通知指南](docs/消息通知模块实现指南.md) | WebSocket + 通知 |
| [部署指南](docs/云服务器部署指南.md) | Docker 部署到云服务器 |
| [修复报告](docs/项目未完成项修复报告.md) | 历史修复记录 |

---

## 部署

详见 [云服务器部署指南](docs/云服务器部署指南.md)。

```bash
# 快速部署
cp .env.example .env && vim .env
mvn package -DskipTests
cd ai-job-market-frontend && npm ci && npm run build && cd ..
docker compose up -d
```

---

## License

MIT
