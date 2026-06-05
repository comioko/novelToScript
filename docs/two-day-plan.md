# 两天开发计划

## 项目概述

**目标**: 在两天内完成 AI 小说转剧本工具的可运行版本。

**核心价值**: 让小说作者快速将小说文本转换为结构化、可编辑的剧本 YAML。

---

## Day 1

### Day 1 上午: 项目骨架与基础接口

**目标**: 搭建 Spring Boot 项目，建立后端目录结构，实现健康检查接口和基本 DTO。

**时间**: 9:00 - 12:00

**具体任务**:

1. 创建 Maven 项目结构
2. 配置 pom.xml 依赖
3. 实现启动类 `Novel2ScriptApplication.java`
4. 实现健康检查接口 `GET /api/health`
5. 创建 DTO 类（Request/Response）
6. 创建静态首页 HTML

**交付物**:
- `pom.xml`
- 项目目录结构
- `HealthController.java`
- 静态 `index.html`
- `style.css`

**验收标准**:
- `mvn spring-boot:run` 可启动
- 浏览器访问 `http://localhost:8080` 可看到页面
- `curl http://localhost:8080/api/health` 返回健康状态

**风险与降级**:
- 如果 Maven 依赖下载慢，预先配置阿里云镜像
- 如果前端页面复杂，先实现简单表单，样式后续完善

---

### Day 1 下午: 小说解析与 YAML 校验

**目标**: 实现章节识别，检查章节数量，实现 YAML 校验服务。

**时间**: 14:00 - 18:00

**具体任务**:

1. 实现 `NovelParserService` 章节解析
2. 支持多种章节标题格式（第X章、Chapter X 等）
3. 实现 `YamlValidationService` 基础校验
4. 实现 `POST /api/scripts/validate` 接口
5. 实现章节数量校验（少于3章报错）

**交付物**:
- `NovelParserService.java`
- `YamlValidationService.java`
- `ScriptController.java` 初版
- `ValidateYamlRequest.java`
- `ValidateYamlResponse.java`

**验收标准**:
- 粘贴3个章节的小说文本，识别出3个章节
- 粘贴2个章节，返回错误提示
- 合法的 YAML 校验通过
- 缺少 `script` 根节点的 YAML 校验失败

**风险与降级**:
- 如果章节识别不稳定，优先支持最常见格式（第X章）
- 如果校验规则复杂，先实现最基础的必填字段检查

---

### Day 1 晚上: AI 调用与 Mock Fallback

**目标**: 实现 AI API 客户端，支持环境变量配置，没有 API Key 时返回 mock YAML。

**时间**: 20:00 - 24:00

**具体任务**:

1. 实现 `AiClient` 发送 Chat Completions 请求
2. 实现 `AiProperties` 读取环境变量
3. 实现 `PromptBuilderService` 构建 prompt
4. 实现 Mock 模式返回示例 YAML
5. 完成 `POST /api/scripts/generate` 主链路

**交付物**:
- `AiClient.java`
- `AiProperties.java`
- `PromptBuilderService.java`
- Mock YAML 示例
- `ScriptGenerationService.java`

**验收标准**:
- 配置 `AI_API_KEY` 时，调用真实 AI API
- 未配置 `AI_API_KEY` 时，返回 Mock YAML 不报错
- Prompt 要求 AI 只返回 YAML，不要解释文字

**风险与降级**:
- 如果 AI API 不稳定，增加错误处理和重试逻辑
- 如果 Prompt 效果不好，先用 Mock 数据验证流程

---

## Day 2

### Day 2 上午: 完整生成链路

**目标**: 打通从输入小说到输出 YAML 的完整流程。

**时间**: 9:00 - 12:00

**具体任务**:

1. 实现 `ScriptGenerationService` 整合所有 Service
2. 实现 YAML 清洗逻辑（去除 markdown 代码块等）
3. 实现自动修复 YAML（如果校验失败）
4. 完善 `GenerateScriptResponse` 返回结构
5. 前后端联调

**交付物**:
- 完整 `ScriptGenerationService`
- `GenerateScriptRequest.java`
- `GenerateScriptResponse.java`
- 完整的 `app.js` 前端交互

**验收标准**:
- 输入3个章节小说文本，输出 YAML 剧本
- YAML 包含 `script` 根节点
- YAML 包含 `characters` 和 `scenes`
- YAML 可被解析

**风险与降级**:
- 如果 AI 输出不稳定，先用 Mock 数据验证
- 如果自动修复复杂，先返回错误让用户手动调整

---

### Day 2 下午: 前端完善与文档

**目标**: 完善 Web 页面交互，编写 README 和 Schema 文档。

**时间**: 14:00 - 18:00

**具体任务**:

1. 完善 `index.html` 布局和样式
2. 实现加载状态展示
3. 实现校验结果展示
4. 实现复制和下载功能
5. 编写 `README.md`
6. 编写 `docs/schema.md`

**交付物**:
- 完整的 `index.html`、`style.css`、`app.js`
- `README.md`
- `docs/schema.md`

**验收标准**:
- 页面美观清晰
- 所有按钮可点击并有响应
- YAML 可复制和下载
- README 可指导他人运行项目

**风险与降级**:
- 如果样式不完美，优先保证功能可用
- 如果文档不完整，先写核心运行指南

---

### Day 2 晚上: 收尾、自测与 Skills 文档

**目标**: 补充 API 文档和 Skills 文档，完成自测，修复明显 bug。

**时间**: 20:00 - 24:00

**具体任务**:

1. 编写 `docs/api.md`
2. 编写 `docs/two-day-plan.md`
3. 编写 `docs/skills.md`
4. 自测所有功能
5. 修复发现的问题
6. 最终验收检查

**交付物**:
- `docs/api.md`
- `docs/two-day-plan.md`
- `docs/skills.md`
- 自测报告

**验收标准**:
- 所有 API 接口可调用
- 少于3章节返回正确错误
- Mock 模式可正常演示
- 无真实 API Key 泄露
- 项目可直接 `mvn spring-boot:run` 运行

**风险与降级**:
- 如果时间紧张，优先保证核心功能，文档可后续补充
- 如果发现 bug，优先修复 P0 功能

---

## 优先级说明

### P0 必须完成

- Spring Boot 项目可运行
- 静态前端页面可访问
- 小说文本输入
- 章节识别，少于3章时报错
- AI API 调用或 mock fallback
- 生成 YAML
- YAML 校验
- 复制和下载
- README.md

### P1 尽量完成

- 自动修复 YAML
- API 文档
- 两天计划文档
- 基础单元测试
- 更好的错误处理

### P2 有时间再做

- 局部重写某个场景
- 导出 Markdown 剧本
- 保存历史记录
- 多模板选择
- 更复杂的长文本摘要链路

---

## 时间分配建议

| 阶段 | 建议时间 | 弹性范围 |
|------|----------|----------|
| Day 1 上午 | 3小时 | ±30分钟 |
| Day 1 下午 | 4小时 | ±1小时 |
| Day 1 晚上 | 4小时 | ±1小时 |
| Day 2 上午 | 3小时 | ±30分钟 |
| Day 2 下午 | 4小时 | ±1小时 |
| Day 2 晚上 | 4小时 | ±1小时 |

---

## 关键风险点

1. **AI API 不稳定**: 使用 Mock Fallback 保证演示
2. **章节识别不准确**: 优先支持最常见格式
3. **YAML 输出不符合 Schema**: 实现自动修复或返回错误
4. **前端交互不流畅**: 优先保证功能可用

---

## 成功标准

项目完成后，必须满足：

1. `mvn spring-boot:run` 可启动
2. `http://localhost:8080` 可访问
3. 输入3个章节文本，可生成 YAML
4. 少于3章节，返回明确错误
5. 无 API Key 泄露
6. README 可指导运行
