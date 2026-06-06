# AI 小说转剧本工具

AI 驱动的工具，将小说文本自动转换为结构化剧本 YAML。

## 功能列表

- 小说文本输入与章节识别（支持第X章、Chapter X 等多种格式）
- 改编参数设置（剧本类型、目标语言、改编风格、目标时长）
- AI 驱动的剧本生成
- YAML 校验与自动修复
- 校验结果展示
- YAML 复制与下载
- Mock Fallback 模式（无 API Key 时可用）

## 技术栈

- **后端**: Java 17, Spring Boot 3.2, Maven
- **前端**: 原生 HTML/CSS/JavaScript
- **数据**: Jackson, SnakeYAML
- **AI**: OpenAI-compatible Chat Completions API

## 目录结构

```
novel2script/
├── pom.xml
├── src/main/java/com/example/novel2script/
│   ├── Novel2ScriptApplication.java
│   ├── controller/
│   │   ├── HealthController.java
│   │   └── ScriptController.java
│   ├── dto/
│   │   ├── AdaptationOptions.java
│   │   ├── ChapterDto.java
│   │   ├── GenerateScriptRequest.java
│   │   ├── GenerateScriptResponse.java
│   │   ├── ValidateYamlRequest.java
│   │   └── ValidateYamlResponse.java
│   ├── service/
│   │   ├── AiClient.java
│   │   ├── NovelParserService.java
│   │   ├── PromptBuilderService.java
│   │   ├── ScriptGenerationService.java
│   │   └── YamlValidationService.java
│   ├── config/
│   │   ├── AiProperties.java
│   │   └── RestClientConfig.java
│   └── exception/
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties
│   └── static/
│       ├── index.html
│       ├── app.js
│       └── style.css
└── docs/
    ├── schema.md
    ├── api.md
    ├── two-day-plan.md
    └── skills.md
```

## 本地运行

### 1. 前提条件

- JDK 17+
- Maven 3.6+

### 2. 配置 AI API（可选）

项目支持通过环境变量配置 AI API。如果不配置，将使用 Mock 模式返回示例剧本。

```bash
export AI_API_KEY=your-api-key
export AI_BASE_URL=https://api.openai.com/v1
export AI_MODEL=gpt-4
```

### 3. 启动服务

```bash
mvn spring-boot:run
```

### 4. 访问页面

打开浏览器访问: http://localhost:8080

## 环境变量配置

| 变量 | 说明 | 默认值 |
|------|------|--------|
| AI_API_KEY | AI API 密钥 | 空（Mock 模式） |
| AI_BASE_URL | API 地址 | https://api.openai.com/v1 |
| AI_MODEL | 模型名称 | gpt-4 |

## 使用流程

1. **输入小说**: 在左侧文本框中粘贴至少 3 个章节的小说文本
2. **设置参数**: 选择剧本类型、目标语言、改编风格和目标时长
3. **生成剧本**: 点击"生成剧本"按钮
4. **查看结果**: 查看 YAML 剧本内容和校验结果
5. **复制/下载**: 使用复制或下载按钮获取结果

## 示例输入

```
第一章 相遇
火星基地外围，黄沙漫天。林浩穿着宇航服在检查一个废弃的采矿探测器。
突然，他发现远处有微弱的闪光。

第二章 流浪行星
两人发现流浪行星的轨迹，意识到它将在三个月内撞击两颗主星。
这个消息让所有人都陷入了恐慌。

第三章 联合行动
林浩和艾琳决定联合两个文明，共同应对宇宙危机。
他们踏上了说服各方势力的旅程。
```

## 示例输出

```yaml
script:
  schema_version: "1.0"
  title: "星辰大海的约定"
  logline: "两个来自不同星球的年轻人在一次意外中相遇，共同面对宇宙危机。"
  synopsis: "2123年，人类已经实现了星际旅行..."
  metadata:
    script_type: "SCREENPLAY"
    target_language: "zh-CN"
    adaptation_style: "DRAMATIC"
    target_duration_minutes: 30
  characters:
    - id: "char_001"
      name: "林浩"
      role: "protagonist"
      ...
  scenes:
    - id: "scene_001"
      title: "火星基地外"
      beats:
        - type: "action"
          content: "火星基地外围，黄沙漫天..."
```

## 已知限制

- 至少需要 3 个章节才能生成剧本
- AI 输出质量依赖输入文本质量
- Mock 模式下返回固定示例 YAML
- 长文本处理采用简单截断，未使用高级摘要技术

## 后续优化方向

- 支持更多章节标题格式
- 增加局部场景重写功能
- 支持导出 Markdown/Final Draft 格式
- 增加历史记录功能
- 引入向量数据库支持超长文本
- 增加流式输出结果
- 支持更多 AI 模型

## API 端点

- `GET /api/health` - 健康检查
- `POST /api/scripts/generate` - 生成剧本
- `POST /api/scripts/validate` - 校验 YAML

详见 [docs/api.md](docs/api.md)
