# YAML Schema 设计文档

## 概述

本项目使用 YAML 作为剧本输出的序列化格式。YAML 是一种人类可读的数据序列化格式，适合配置文件和剧本这类需要人工阅读和编辑的场景。

## 为什么使用 YAML

1. **人类可读**: 与 JSON 相比，YAML 更适合表示配置文件和文档类数据
2. **可编辑**: 编剧可以直接修改 YAML 文件进行润色
3. **结构化**: 支持嵌套结构，适合表示剧本的层级关系
4. **广泛支持**: 主流编程语言都有良好的 YAML 解析库

## 根节点设计

### 为什么使用 `script` 作为根节点

使用 `script` 作为根节点有以下考虑：

1. **语义明确**: `script` 直接表示"剧本"，与项目目标一致
2. **避免冲突**: 使用通用字段名容易与其他配置文件冲突
3. **扩展性**: 未来可以在根节点添加 `metadata`、`version` 等字段而不影响兼容性

```yaml
script:  # 根节点
  schema_version: "1.0"
  title: "..."
```

## 关键字段说明

### schema_version

```yaml
schema_version: "1.0"
```

- **为什么需要**: 剧本格式可能演进，版本号允许未来兼容旧格式
- **设计原因**: 使用字符串而非数字，便于显示（如 "1.0"、"2.1"）

### metadata

```yaml
metadata:
  script_type: "SCREENPLAY"
  target_language: "zh-CN"
  adaptation_style: "DRAMATIC"
  target_duration_minutes: 30
  generated_at: "2026-06-05T10:00:00+08:00"
```

- **为什么需要**: 元信息描述剧本的生成上下文，便于分类和检索
- **设计原因**:
  - `script_type` 区分不同类型剧本
  - `target_language` 支持多语言
  - `adaptation_style` 记录改编策略
  - `generated_at` 追踪生成时间

### source

```yaml
source:
  type: "novel"
  chapter_count: 3
  chapters:
    - id: "chapter_001"
      index: 1
      title: "第一章"
      summary: "本章摘要"
```

- **为什么需要**: 保留原始小说信息，便于追溯和版权说明
- **设计原因**:
  - `type` 标记来源类型（小说、原创等）
  - `chapters` 记录章节结构和摘要
  - 便于后续扩展支持"基于 XX 小说改编"

### characters

```yaml
characters:
  - id: "char_001"
    name: "角色名"
    role: "protagonist"
    description: "角色描述"
    motivation: "角色动机"
    relationships:
      - target_character_id: "char_002"
        relation: "朋友"
```

- **为什么需要**: 人物是剧本核心，需要集中管理
- **设计原因**:
  - `id` 使用唯一标识符，便于引用
  - `role` 标记角色类型（protagonist/antagonist/supporting）
  - `motivation` 记录角色动机，有助于保持角色一致性
  - `relationships` 使用 `target_character_id` 而非直接嵌套，保持结构扁平

### scenes

```yaml
scenes:
  - id: "scene_001"
    title: "场景标题"
    source_chapters: ["chapter_001"]
    location: "地点"
    time: "时间"
    characters: ["char_001"]
    summary: "场景摘要"
    dramatic_function: "该场景在剧作结构中的作用"
    beats:
      - type: "dialogue"
        character_id: "char_001"
        character_name: "角色名"
        content: "对白内容"
```

- **为什么需要**: 场景是剧本的基本叙事单元
- **设计原因**:
  - `source_chapters` 使用数组，支持一个场景跨多个章节
  - `location` 和 `time` 支持场景快速检索
  - `dramatic_function` 帮助编剧理解场景在整体结构中的作用

### beats

beat 是剧本的最小叙事单元，设计参考 Final Draft 和专业编剧软件：

| type | 说明 | 必需字段 |
|------|------|----------|
| action | 动作/场景描写 | content |
| dialogue | 对白 | content, character_id, character_name |
| narration | 旁白 | content |
| transition | 转场 | content |
| sound | 音效 | content |
| camera | 镜头指示 | content |

- **为什么需要**: 细化到 beat 级别便于程序处理和格式转换
- **设计原因**:
  - `type` 区分不同类型的叙事单元
  - `dialogue` 必须包含角色信息，便于后续处理（如语音合成）
  - 支持扩展更多 beat 类型

### notes

```yaml
notes:
  adaptation_notes:
    - "将小说中的心理描写转化为动作和对白。"
  potential_improvements:
    - "后续可以进一步强化第二幕冲突。"
```

- **为什么需要**: 记录改编笔记和优化建议
- **设计原因**: 剧本是迭代产品，notes 便于人工继续打磨

## ID 引用机制

角色和场景使用 `id` 而非名称引用：

```yaml
beats:
  - type: "dialogue"
    character_id: "char_001"  # 使用 ID 而非名称
    character_name: "角色名"
```

- **为什么使用 ID**:
  - 角色可能重名，ID 保证唯一性
  - 便于程序处理和验证引用完整性
  - 修改角色名时只需改一处

## 扩展性设计

### 支持未来扩展

1. **新增 beat 类型**: 添加新 type 不影响现有解析
2. **新增 metadata 字段**: 追加到 metadata 对象
3. **新增角色属性**: 在 character 对象中追加
4. **多版本 Schema**: 通过 schema_version 区分

### 不支持的场景

- 移除必需字段（会破坏兼容性）
- 修改字段语义（如把 title 变成数组）

## 完整 Schema 示例

详见主 README 或示例输出。
