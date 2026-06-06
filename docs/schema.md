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
  - `script_type` 区分不同类型剧本（SCREENPLAY/DRAMATIC/SHORT_DRAMA/RADIO_DRAMA）
  - `target_language` 支持多语言
  - `adaptation_style` 记录改编策略（FAITHFUL/DRAMATIC/COMMERCIAL/SUSPENSEFUL/COMEDIC）
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
    name: "林晚"
    role: "protagonist"
    description: "28岁，旧书店店员，性格倔强执着。"
    motivation: "找到父亲失踪的真相"
    relationships:
      - target_character_id: "char_002"
        relation: "父女"
        arc: "父亲失踪后，通过遗物逐渐拼凑真相"
```

- **为什么需要**: 人物是剧本核心，需要集中管理
- **设计原因**:
  - `id` 使用唯一标识符（char_001, char_002...），便于引用
  - `role` 标记角色类型（protagonist/antagonist/supporting）
  - `description` 包含年龄、身份、性格特点
  - `motivation` 记录角色动机，有助于保持角色一致性
  - `relationships` 使用 `target_character_id` 而非直接嵌套，保持结构扁平
  - `arc` 字段描述关系变化过程（如"陌生人 -> 盟友 -> 彼此信任"）

### scenes

```yaml
scenes:
  - id: "scene_001"
    title: "雨夜书店"
    source_chapters: ["chapter_001"]
    location: "南城旧书店"
    time: "雨夜，傍晚，打烊时分"
    characters: ["char_001", "char_002"]
    summary: "林晚在旧书店发现父亲字迹的神秘来信。"
    dramatic_function: "建立主角处境，埋下悬念"
    beats:
      - type: "action"
        content: "雨声潺潺，镜头缓缓推近旧书店门口。"
      - type: "dialogue"
        character_id: "char_001"
        character_name: "林晚"
        content: "你凭什么管我？"
      - type: "camera"
        content: "镜头从信封缓缓拉近。"
      - type: "sound"
        content: "雷声炸响。"
      - type: "transition"
        content: "切至：同一日，午夜十二点。"
```

- **为什么需要**: 场景是剧本的基本叙事单元
- **设计原因**:
  - `source_chapters` 使用数组，支持一个场景跨多个章节
  - `location` 和 `time` 支持场景快速检索
  - `dramatic_function` 帮助编剧理解场景在整体结构中的作用
  - **重要**: `characters` 列表必须包含该场景所有会说话的角色 ID

#### scene.characters 完整性规则

`scene.characters` 是**最容易出错的字段**，必须严格执行：

**规则**: 如果某个 scene 的 dialogue beat 中出现了某角色，该角色的 ID 必须出现在该 scene 的 characters 列表中。

**正确示例**:
```yaml
scenes:
  - id: "scene_003"
    title: "地下室"
    characters: ["char_001", "char_002", "char_003"]  # 包含所有会说话的角色
    beats:
      - type: "dialogue"
        character_id: "char_001"
        character_name: "林晚"
        content: "..."
      - type: "dialogue"
        character_id: "char_003"
        character_name: "周柏"
        content: "..."
```

**错误示例**:
```yaml
scenes:
  - id: "scene_003"
    title: "地下室"
    characters: ["char_001"]  # 错误：遗漏了 char_003
    beats:
      - type: "dialogue"
        character_id: "char_003"  # 但这里用了 char_003
        character_name: "周柏"
        content: "..."
```

### beats

beat 是剧本的最小叙事单元，设计参考 Final Draft 和专业编剧软件：

| type | 说明 | 必需字段 | character_id |
|------|------|----------|--------------|
| action | 动作/场景描写 | content | 不需要 |
| dialogue | 对白 | content, character_id, character_name | 必须 |
| narration | 旁白 | content | 不需要 |
| transition | 转场 | content | 不需要 |
| sound | 音效 | content | 不需要 |
| camera | 镜头指示 | content | 不需要 |

- **为什么需要**: 细化到 beat 级别便于程序处理和格式转换
- **设计原因**:
  - `type` 区分不同类型的叙事单元
  - `dialogue` 必须包含 character_id 和 character_name，便于后续处理（如语音合成）
  - 支持扩展更多 beat 类型

#### dialogue beat 格式规范

**content 只写台词本身**，不要重复写说话人前缀：

**正确**:
```yaml
- type: "dialogue"
  character_id: "char_001"
  character_name: "林晚"
  content: "你凭什么管我？"
```

**错误**:
```yaml
- type: "dialogue"
  character_id: "char_001"
  character_name: "林晚"
  content: "林晚：'你凭什么管我？'"  # 错误：重复了说话人
```

### notes

```yaml
notes:
  adaptation_notes:
    - "将小说中的心理描写转化为动作和对白。"
    - "增加了顾沉受伤保护林晚的动作场面。"
  potential_improvements:
    - "可以进一步强化第二幕冲突。"
    - "可以增加周柏的背景故事。"
```

- **为什么需要**: 记录改编笔记和优化建议
- **设计原因**: 剧本是迭代产品，notes 便于人工继续打磨

## ID 引用机制

角色使用 `id` 而非名称引用：

```yaml
beats:
  - type: "dialogue"
    character_id: "char_001"  # 使用 ID 而非名称
    character_name: "林晚"
    content: "对白内容"
```

- **为什么使用 ID**:
  - 角色可能重名，ID 保证唯一性
  - 便于程序处理和验证引用完整性
  - 修改角色名时只需改一处

### 角色 ID 分配规则

- protagonist（主角）: char_001, char_002...
- antagonist（反派）: char_005, char_006...
- supporting（配角）: char_003, char_004...

**重要**: 同一 character_id 只能对应一个角色名。禁止让 char_005 既当"幕后声音"又当"黑衣人"，必须分配不同的 ID。

## time 字段格式规范

time 字段必须与故事背景匹配：

| 故事类型 | 正确示例 | 错误示例 |
|----------|----------|----------|
| 现代/现实 | "雨夜，傍晚" | "火星日第184日" |
| 现代/现实 | "同一日，午夜十二点" | "星际标准时间09:00" |
| 现代/现实 | "三天后，上午十点" | "23世纪标准时间" |
| 科幻 | "火星日第184日，午后" | - |
| 科幻 | "星历2347年，标准时09:00" | - |

**规则**: 如果是现代/现实题材，禁止使用科幻时间格式。

## 扩展性设计

### 支持未来扩展

1. **新增 beat 类型**: 添加新 type 不影响现有解析
2. **新增 metadata 字段**: 追加到 metadata 对象
3. **新增角色属性**: 在 character 对象中追加
4. **多版本 Schema**: 通过 schema_version 区分

### 不支持的场景

- 移除必需字段（会破坏兼容性）
- 修改字段语义（如把 title 变成数组）
- 让同一 ID 对应多个不同角色名

## 完整 Schema 示例

```yaml
script:
  schema_version: "1.0"
  title: "长明剧场"
  logline: "十年后，女儿林晚收到神秘来信，追寻失踪父亲的真相。"
  synopsis: "南城雨夜，旧书店店员林晚发现父亲字迹的来信..."
  metadata:
    script_type: "SCREENPLAY"
    target_language: "zh-CN"
    adaptation_style: "FAITHFUL"
    target_duration_minutes: 30
    generated_at: "2026-06-06T10:00:00+08:00"
  source:
    type: "novel"
    chapter_count: 3
    chapters:
      - id: "chapter_001"
        index: 1
        title: "雨夜来信"
        summary: "林晚在旧书店发现父亲字迹的来信。"
  characters:
    - id: "char_001"
      name: "林晚"
      role: "protagonist"
      description: "女，二十八岁，旧书店店员，性格倔强执着。"
      motivation: "找到父亲失踪的真相"
      relationships:
        - target_character_id: "char_002"
          relation: "父女"
          arc: "通过遗物逐渐拼凑真相"
  scenes:
    - id: "scene_001"
      title: "雨夜书店"
      source_chapters: ["chapter_001"]
      location: "南城旧书店"
      time: "雨夜，傍晚，打烊时分"
      characters: ["char_001"]
      summary: "林晚发现父亲字迹的神秘来信。"
      dramatic_function: "建立悬念，介绍主角"
      beats:
        - type: "action"
          content: "雨声潺潺，镜头缓缓推近旧书店门口。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林晚"
          content: "书店快要打烊了。"
        - type: "transition"
          content: "切至：同一日，午夜十二点。"
  notes:
    adaptation_notes: []
    potential_improvements: []
```
