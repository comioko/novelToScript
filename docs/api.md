# API 文档

## 概述

本项目提供 RESTful API，用于小说转剧本生成和 YAML 校验。

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`

---

## 健康检查

### GET /api/health

检查服务健康状态。

**响应示例**:

```json
{
  "status": "UP",
  "service": "novel2script",
  "timestamp": "2026-06-05T10:00:00+08:00"
}
```

**响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| status | string | 服务状态，UP 表示正常 |
| service | string | 服务名称 |
| timestamp | string | 服务器时间（ISO 8601） |

---

## 生成剧本

### POST /api/scripts/generate

将小说文本转换为剧本 YAML。

**请求示例**:

```json
{
  "novelText": "第一章 相遇\n火星基地外围，黄沙漫天...\n\n第二章 流浪行星\n两人发现流浪行星...\n\n第三章 联合行动\n林浩和艾琳决定联合...",
  "scriptType": "SCREENPLAY",
  "targetLanguage": "zh-CN",
  "adaptationStyle": "DRAMATIC",
  "targetDurationMinutes": 30
}
```

**请求字段**:

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| novelText | string | 是 | 小说文本，至少3个章节 |
| scriptType | string | 否 | 剧本类型，默认 SCREENPLAY |
| targetLanguage | string | 否 | 目标语言，默认 zh-CN |
| adaptationStyle | string | 否 | 改编风格，默认 DRAMATIC |
| targetDurationMinutes | int | 否 | 目标时长，默认30 |

**scriptType 可选值**:

- `SCREENPLAY`: 影视剧本
- `DRAMATIC`: 戏剧剧本
- `SHORT_DRAMA`: 短剧剧本
- `RADIO_DRAMA`: 广播剧剧本

**targetLanguage 可选值**:

- `zh-CN`: 中文
- `en-US`: 英文

**adaptationStyle 可选值**:

- `FAITHFUL`: 忠于原著
- `DRAMATIC`: 强化戏剧冲突
- `COMMERCIAL`: 更商业化
- `SUSPENSEFUL`: 更悬疑
- `COMEDIC`: 更轻喜剧

**响应示例（成功）**:

```json
{
  "success": true,
  "message": "剧本生成成功",
  "yaml": "script:\n  schema_version: \"1.0\"\n  title: \"星辰大海的约定\"...",
  "validation": {
    "valid": true,
    "errors": [],
    "warnings": []
  },
  "chapters": [
    {
      "index": 1,
      "title": "第一章 相遇",
      "contentPreview": "火星基地外围，黄沙漫天..."
    }
  ]
}
```

**响应示例（失败 - 章节不足）**:

```json
{
  "success": false,
  "message": "检测到 2 个章节，但至少需要 3 个章节才能生成剧本",
  "yaml": null,
  "validation": null,
  "chapters": [...]
}
```

**HTTP 状态码**:

| 状态码 | 说明 |
|--------|------|
| 200 | 生成成功 |
| 400 | 请求参数错误或章节不足 |

---

## 校验 YAML

### POST /api/scripts/validate

校验 YAML 剧本是否符合 Schema 要求。

**请求示例**:

```json
{
  "yaml": "script:\n  schema_version: \"1.0\"\n  title: \"示例剧本\"..."
}
```

**请求字段**:

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| yaml | string | 是 | YAML 内容 |

**响应示例（合法）**:

```json
{
  "valid": true,
  "errors": [],
  "warnings": []
}
```

**响应示例（不合法）**:

```json
{
  "valid": false,
  "errors": [
    "Missing required root key 'script'",
    "Missing required field 'script.characters'"
  ],
  "warnings": [
    "'script.title' is empty"
  ]
}
```

**HTTP 状态码**:

| 状态码 | 说明 |
|--------|------|
| 200 | YAML 合法 |
| 400 | YAML 不合法 |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| VALIDATION_ERROR | 请求参数校验失败 |
| BUSINESS_ERROR | 业务逻辑错误 |
| AI_API_ERROR | AI API 调用失败 |
| AI_RESPONSE_ERROR | AI 响应解析失败 |
| INTERNAL_ERROR | 内部服务器错误 |

---

## 错误响应格式

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "小说文本不能为空",
  "errors": {
    "novelText": "must not be blank"
  }
}
```
