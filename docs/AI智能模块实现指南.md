# AI 智能模块实现指南

> 项目: AI 智能招聘市场 | 模块: AI 智能模块（求职助手/模拟面试/简历分析） | 技术栈: Spring AI + DashScope(Qwen) + pgvector + Redis

---

## 目录

1. [已有基础设施总览](#1-已有基础设施总览)
2. [整体流程概览](#2-整体流程概览)
3. [第 1 步：Entity 实体类](#3-第-1-步entity-实体类)
4. [第 2 步：Mapper + Service](#4-第-2-步mapper--service)
5. [第 3 步：Controller](#5-第-3-步controller)
6. [第 4 步：AI 模拟面试](#6-第-4-步ai-模拟面试)
7. [完整架构图](#7-完整架构图)
8. [前端页面设计](#8-前端页面设计)
9. [创建文件清单速查](#9-创建文件清单速查)

---

## 1. 已有基础设施总览

> **本模块无需从零搭建。** 先了解已有组件，再设计新功能。

### 1.1 AI 核心引擎

| 组件 | 路径 | 功能 |
|------|------|------|
| `JobApp` | `AI/app/JobApp.java` | 统一AI入口：doChat/doChatByStream/doChatWithRag/doChatWithTools/doChatWithMcp |
| `JobAgent` | `AI/agent/JobAgent.java` | ReAct+ToolCall Agent，maxSteps=20，全工具集 |
| `BaseAgent` | `AI/agent/BaseAgent.java` | Agent基类，run()/runStream()/step() |
| `ChatClient` | JobApp内部 | 基于dashScopeChatModel(Qwen)，带MessageChatMemoryAdvisor |

### 1.2 对话记忆

| 组件 | 存储 | 用途 |
|------|------|------|
| `RedisBasedChatMemory` | Redis `chat:memory:{id}` | 生产环境多轮对话持久化 |
| `FileBasedChatMemory` | 文件 `{dir}/{id}.kryo` | 备选/本地调试 |

### 1.3 RAG 管道（需启用）

| 组件 | 功能 | 状态 |
|------|------|------|
| `JobAppVectorStoreConfig` | 内存向量库(DashScope Embedding) | 已配置 |
| `JobAppDocumentLoader` | 加载 `classpath:document/*.md` | 已配置 |
| `QueryRewriter` | AI查询重写 | 已配置 |
| `JobAppRagCustomAdvisorFactory` | 自定义RAG增强 | 代码完整 |
| `doChatWithRag()` | JobApp中RAG对话方法 | 已实现但注释 |

### 1.4 工具集（7个）

| 工具 | 功能 |
|------|------|
| `SearchTool` | 百度千帆AI搜索 |
| `WebScrapingTool` | Jsoup网页抓取 |
| `FileOperationTool` | 文件读写 |
| `PDFGenerationTool` | PDF生成/提取/合并/拆分 |
| `ResourceDownloadTool` | URL资源下载 |
| `TerminalOperationTool` | 安全终端命令执行 |
| `TerminateTool` | 终止Agent交互 |

### 1.5 已有 AI 数据库表

| 表 | 状态 | 说明 |
|----|------|------|
| `ai_chat_session` | 已建表 | 会话管理 |
| `ai_chat_message` | 已建表 | 消息记录 |
| `ai_resume_analysis` | 已建表+Entity+Mapper | AI简历分析（已部分实现） |
| `ai_job_match` | 已建表 | AI职位匹配 |
| `ai_interview_record` | 已建表 | AI模拟面试记录 |

### 1.6 已嵌入业务流的 AI 功能

| 功能 | 位置 | 说明 |
|------|------|------|
| AI 匹配度计算 | `ApplicationServiceImpl.apply()` | 投递时自动计算技能重合 |
| AI 简历分析 | `ResumeServiceImpl.aiAnalyze()` | 调用JobApp分析简历 |
| AI 简历优化 | `ResumeServiceImpl.aiOptimize()` | 生成优化建议 |

---

## 2. 整体流程概览

### 2.1 AI 求职助手流程

```
用户打开 AI 聊天 → POST /api/ai/chat/sessions { type: "CAREER_ADVICE" }
  → 返回 sessionId
  
用户发消息 → POST /api/ai/chat/send { sessionId, message }
  → 查会话历史
  → 调用 jobApp.doChat(message, chatId)  ← Redis 自动管理多轮记忆
  → 保存 ai_chat_message (user + assistant)
  → 返回 AI 回复
```

### 2.2 AI 模拟面试流程

```
用户选择目标职位 → POST /api/ai/interview/start { jobId }
  → 查职位 JD (description + requirement)
  → AI 生成第1道面试题
  → 创建 ai_interview_record (question)
  → 返回 { recordId, question }

用户回答 → POST /api/ai/interview/answer { recordId, answer }
  → AI 评估回答质量
  → 返回 { score, feedback }
  → 生成下一道题（循环5-10轮）

查看报告 → GET /api/ai/interview/{sessionId}/report
  → 汇总所有答题记录
  → 返回 { totalScore, questions[], suggestions }
```

### 2.3 关键设计决策（与已有模块保持一致）

| 决策点 | 方案 | 原因 |
|--------|------|------|
| 会话管理 | DB表 + Redis记忆双重存储 | DB用于列表展示，Redis用于LLM上下文 |
| 持久化 | `chatId = "ai-chat-" + sessionId` | 逻辑清晰，restart不丢失 |
| 面试出题 | 从 JD + 分类题库动态生成 | 灵活应对不同职位 |
| 消息存储 | 保存 user/assistant 消息，不计 system prompt | 减少Token浪费 |

---

## 3. 第 1 步：Entity 实体类

### 3.1 `AiChatSession.java`

```java
@Data @TableName("ai_chat_session")
public class AiChatSession implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String sessionType;   // CAREER_ADVICE/RESUME_HELP/INTERVIEW_PRACTICE/GENERAL
    private String title;         // 会话标题
    private Integer messageCount;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

### 3.2 `AiChatMessage.java` / `AiInterviewRecord.java` / `AiJobMatch.java`

同理，字段与对应数据库表一致。

---

## 4. 第 2 步：Mapper + Service

```java
// 4个 Mapper 全部继承 BaseMapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {}
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {}
public interface AiInterviewRecordMapper extends BaseMapper<AiInterviewRecord> {}
public interface AiJobMatchMapper extends BaseMapper<AiJobMatch> {}

// AiChatService 接口
public interface AiChatService {
    Long createSession(Long userId, String type, String title);
    List<AiChatSession> listSessions(Long userId);
    String sendMessage(Long sessionId, Long userId, String message);
    List<AiChatMessage> getMessages(Long sessionId);
}

// AiChatServiceImpl 核心逻辑
@Service
public class AiChatServiceImpl implements AiChatService {

    @Resource private JobApp jobApp;
    @Resource private AiChatSessionMapper sessionMapper;
    @Resource private AiChatMessageMapper messageMapper;

    @Override
    public String sendMessage(Long sessionId, Long userId, String message) {
        // 1. 保存用户消息到 DB
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("USER");
        userMsg.setContent(message);
        messageMapper.insert(userMsg);

        // 2. 调用 AI (chatId = "ai-chat-" + sessionId，Redis 自动记忆)
        String reply = jobApp.doChat(message, "ai-chat-" + sessionId);

        // 3. 保存 AI 回复
        AiChatMessage aiMsg = new AiChatMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("ASSISTANT");
        aiMsg.setContent(reply);
        messageMapper.insert(aiMsg);

        // 4. 更新会话消息数
        AiChatSession session = sessionMapper.selectById(sessionId);
        session.setMessageCount(session.getMessageCount() + 1);
        sessionMapper.updateById(session);

        return reply;
    }
}
```

**关键点**：`JobApp.doChat()` 内部使用 `chatId` 参数，由 `RedisBasedChatMemory` 管理的 `MessageChatMemoryAdvisor` 自动处理多轮对话上下文。无需手动传历史消息。

---

## 5. 第 3 步：Controller

### 5.1 `AiChatController.java`

```java
@RestController @RequestMapping("/ai")
public class AiChatController {

    @Resource private AiChatService aiChatService;
    @Resource private AiInterviewService aiInterviewService;
    @Resource private UserService userService;

    // === 会话 ===
    @PostMapping("/chat/sessions")
    public BaseResponse<Long> createSession(@RequestBody CreateSessionRequest req, HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(aiChatService.createSession(user.getId(), req.getType(), req.getTitle()));
    }

    @GetMapping("/chat/sessions")
    public BaseResponse<List<AiChatSession>> listSessions(HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(aiChatService.listSessions(user.getId()));
    }

    @GetMapping("/chat/sessions/{id}/messages")
    public BaseResponse<List<AiChatMessage>> messages(@PathVariable Long id) {
        return ResultUtils.success(aiChatService.getMessages(id));
    }

    @PostMapping("/chat/send")
    public BaseResponse<String> sendMessage(@RequestBody SendMessageRequest req, HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(aiChatService.sendMessage(req.getSessionId(), user.getId(), req.getMessage()));
    }

    // === AI 模拟面试 ===
    @PostMapping("/interview/start")
    public BaseResponse<AiInterviewRecord> startInterview(@RequestBody InterviewStartRequest req, HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(aiInterviewService.startInterview(user.getId(), req.getJobId()));
    }

    @PostMapping("/interview/answer")
    public BaseResponse<InterviewAnswerResponse> answerQuestion(@RequestBody InterviewAnswerRequest req, HttpServletRequest request) {
        UserVO user = userService.getLoginUser(request);
        return ResultUtils.success(aiInterviewService.evaluateAnswer(req.getRecordId(), user.getId(), req.getAnswer()));
    }

    @GetMapping("/interview/{sessionId}/report")
    public BaseResponse<InterviewReport> getReport(@PathVariable Long sessionId) {
        return ResultUtils.success(aiInterviewService.getReport(sessionId));
    }
}
```

**API 路由总览：**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/ai/chat/sessions` | 新建会话 |
| GET | `/api/ai/chat/sessions` | 会话列表 |
| GET | `/api/ai/chat/sessions/{id}/messages` | 消息历史 |
| POST | `/api/ai/chat/send` | 发送消息→AI回复 |
| POST | `/api/ai/interview/start` | 开始AI模拟面试 |
| POST | `/api/ai/interview/answer` | 提交回答+AI评分 |
| GET | `/api/ai/interview/{sessionId}/report` | 面试报告 |
| GET | `/ai/chat/stream` | SSE流式对话(复用AiController) |

---

## 6. 第 4 步：AI 模拟面试

### 6.1 `AiInterviewService.java`

```java
public interface AiInterviewService {
    AiInterviewRecord startInterview(Long userId, Long jobId);
    InterviewAnswerResponse evaluateAnswer(Long recordId, Long userId, String answer);
    InterviewReport getReport(Long sessionId);
}
```

### 6.2 核心逻辑

```java
@Override
public AiInterviewRecord startInterview(Long userId, Long jobId) {
    Job job = jobMapper.selectById(jobId);
    ThrowUtils.throwIf(job == null, ErrorCode.NOT_FOUND_ERROR, "职位不存在");

    String prompt = """
        你是一位资深技术面试官。请根据以下职位描述生成一道专业面试题。
        职位：%s
        要求：%s
        生成一道技术面试题，包含题目和期望答案关键词(逗号分隔)。
        以 JSON 返回：{"question":"...", "keywords":"...", "type":"TECHNICAL"}
        只返回 JSON。
        """.formatted(job.getTitle(), StringUtils.defaultString(job.getRequirement(), job.getDescription()));

    String result = jobApp.doChat(prompt, "interview-gen-" + jobId);

    // 解析 AI 返回的 JSON，提取 question 和 keywords
    // 创建 ai_interview_record 并保存
    AiInterviewRecord record = new AiInterviewRecord();
    record.setUserId(userId);
    record.setJobId(jobId);
    record.setQuestion(question);
    record.setExpectedAnswerKeywords(keywords);
    record.setQuestionIndex(1);
    interviewRecordMapper.insert(record);
    return record;
}

@Override
public InterviewAnswerResponse evaluateAnswer(Long recordId, Long userId, String answer) {
    AiInterviewRecord record = interviewRecordMapper.selectById(recordId);

    String prompt = """
        你是一位资深面试官。请评估以下面试回答。
        题目：%s
        期望关键词：%s
        回答：%s
        给出 0-100 的评分和具体的改进建议。
        以 JSON 返回：{"score":85, "feedback":"..."}
        """.formatted(record.getQuestion(), record.getExpectedAnswerKeywords(), answer);

    String result = jobApp.doChat(prompt, "interview-eval-" + recordId);

    // 解析评分，更新记录
    record.setUserAnswer(answer);
    record.setScore(parsedScore);
    record.setAiFeedback(parsedFeedback);
    interviewRecordMapper.updateById(record);

    // 生成下一题
    AiInterviewRecord nextQuestion = generateNextQuestion(userId, record.getJobId(), record.getQuestionIndex() + 1);

    return new InterviewAnswerResponse(parsedScore, parsedFeedback, nextQuestion);
}
```

### 6.3 面试报告

```java
@Override
public InterviewReport getReport(Long sessionId) {
    List<AiInterviewRecord> records = interviewRecordMapper.selectList(
        new LambdaQueryWrapper<AiInterviewRecord>().eq(AiInterviewRecord::getSessionId, sessionId));

    int totalScore = records.stream().filter(r -> r.getScore() != null)
            .mapToInt(AiInterviewRecord::getScore).sum();
    int count = (int) records.stream().filter(r -> r.getScore() != null).count();

    InterviewReport report = new InterviewReport();
    report.setTotalQuestions(records.size());
    report.setAverageScore(count > 0 ? totalScore / count : 0);
    report.setRecords(records);
    report.setSuggestion(count > 80 ? "表现优秀，建议继续投递" : "建议针对性提升薄弱环节后再次尝试");
    return report;
}
```

---

## 7. 完整架构图

```
┌──────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3)                           │
│  /ai/chat 求职助手    /ai/interview 模拟面试                 │
│  /resumes/{id}/analysis AI简历分析(已有)                     │
└──────────────────────────┬───────────────────────────────────┘
                           │ REST API
┌──────────────────────────▼───────────────────────────────────┐
│                    AiChatController.java                      │
│  @RequestMapping("/ai")                                      │
│  会话管理 + 消息收发 + 模拟面试                               │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────▼───────────────────────────────────┐
│                      Service 层                               │
│  AiChatService: sendMessage() → jobApp.doChat()              │
│  AiInterviewService: startInterview() → evaluateAnswer()     │
│  已有: ResumeService.aiAnalyze() / .aiOptimize()             │
└──────┬───────────────────────────────────────────────────────┘
       │
┌──────▼──────────────────────────────────────────────────────┐
│                     AI 核心引擎 (已有)                        │
│  JobApp (6种对话模式) │ JobAgent (ReAct+ToolCall)            │
│  RedisBasedChatMemory  │  RAG管道(5组件)  │  7个工具         │
└─────────────────────────────────────────────────────────────┘
       │
┌──────▼──────┐    ┌──────────────┐    ┌──────────────────┐
│  DB 持久化   │    │  Redis 记忆   │    │  DashScope       │
│  ai_chat_*  │    │  chat:memory │    │  Qwen模型         │
│  ai_interview│   │  (多轮对话)   │    │  1536维Embedding │
└─────────────┘    └──────────────┘    └──────────────────┘
```

---

## 8. 前端页面设计

### 8.1 AiChat.vue — AI 求职助手

```
┌─────────────────────────────────────────┐
│  AI 求职助手                    [新会话] │
├──────────┬──────────────────────────────┤
│ 会话列表  │  消息区域                     │
│ ├ 求职咨询│  ┌─────────────────────┐     │
│ ├ 简历帮助│  │ AI: 你好，我是AI求职 │     │
│ └ 面试练习│  │ 助手，可以帮你...    │     │
│          │  └─────────────────────┘     │
│          │  ┌─────────────────────┐     │
│          │  │ 用户: Java后端的...  │     │
│          │  └─────────────────────┘     │
│          │                              │
│          │  [输入框____________] [发送]  │
└──────────┴──────────────────────────────┘
```

功能要点：
- 左侧会话列表(从API获取)，可切换/新建
- 右侧消息区，user/assistant消息气泡
- 底部输入框+发送按钮
- 加载历史消息

### 8.2 AiInterview.vue — AI 模拟面试

```
┌─────────────────────────────────────────┐
│  AI 模拟面试                             │
│  目标职位: [选择职位下拉框]    [开始面试] │
├─────────────────────────────────────────┤
│  第 3/10 题                              │
│  ┌─────────────────────────────────┐    │
│  │ Q: 请描述你在项目中使用Redis     │    │
│  │ 解决缓存穿透的方案。             │    │
│  └─────────────────────────────────┘    │
│  你的回答:                               │
│  ┌─────────────────────────────────┐    │
│  │ [textarea___________________]   │    │
│  └─────────────────────────────────┘    │
│  [提交回答]                              │
│                                         │
│  AI 评分: 85/100                         │
│  反馈: 回答完整，但可以补充...            │
│  ─────────────────────────────          │
│  [下一题]  [查看报告]                    │
└─────────────────────────────────────────┘
```

### 8.3 前端路由

| 路径 | 页面 | 功能 |
|------|------|------|
| `/ai/chat` | AiChat.vue | AI求职助手对话 |
| `/ai/interview` | AiInterview.vue | AI模拟面试 |
| `/resumes/{id}/analysis` | AIAnalysis.vue | AI简历分析报告(已有API) |

### 8.4 api.js 新增

```js
export const aiApi = {
  createSession: (body) => request('/ai/chat/sessions', { method: 'POST', body: JSON.stringify(body) }),
  listSessions: () => request('/ai/chat/sessions'),
  getMessages: (id) => request('/ai/chat/sessions/' + id + '/messages'),
  sendMessage: (body) => request('/ai/chat/send', { method: 'POST', body: JSON.stringify(body) }),
  startInterview: (body) => request('/ai/interview/start', { method: 'POST', body: JSON.stringify(body) }),
  answerInterview: (body) => request('/ai/interview/answer', { method: 'POST', body: JSON.stringify(body) }),
  getInterviewReport: (id) => request('/ai/interview/' + id + '/report'),
}
```

---

## 9. 创建文件清单速查

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `model/entity/AiChatSession.java` | 会话实体 |
| 2 | `model/entity/AiChatMessage.java` | 消息实体 |
| 3 | `model/entity/AiInterviewRecord.java` | 面试记录实体 |
| 4 | `model/entity/AiJobMatch.java` | 匹配结果实体 |
| 5 | `mapper/AiChatSessionMapper.java` | |
| 6 | `mapper/AiChatMessageMapper.java` | |
| 7 | `mapper/AiInterviewRecordMapper.java` | |
| 8 | `mapper/AiJobMatchMapper.java` | |
| 9 | `service/AiChatService.java` | AI聊天服务接口 |
| 10 | `service/AiInterviewService.java` | AI面试服务接口 |
| 11 | `service/impl/AiChatServiceImpl.java` | AI聊天实现(核心!) |
| 12 | `service/impl/AiInterviewServiceImpl.java` | AI面试实现 |
| 13 | `controller/AiChatController.java` | 统一AI控制器 |
| 14 | `ai-job-market-frontend/src/pages/AiChat.vue` | 前端:AI聊天页 |
| 15 | `ai-job-market-frontend/src/pages/AiInterview.vue` | 前端:AI面试页 |
| 16 | `ai-job-market-frontend/src/api.js` | 改:+aiApi |
| 17 | `ai-job-market-frontend/src/main.js` | 改:+AI路由 |

**共 17 个文件（后端 12 新建 + 前端 3 新建 + 2 修改）。**
