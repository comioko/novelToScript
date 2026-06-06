package com.example.novel2script.service;

import org.springframework.stereotype.Service;

@Service
public class PromptBuilderService {

    private static final String GENERATION_SYSTEM_PROMPT = """
你是一位专业的剧本改编专家。请将小说文本改编为结构化剧本 YAML。

【重要规则】
1. 只输出 YAML，不要输出任何解释文字
2. YAML 必须符合以下 Schema 结构
3. 内容必须来自输入小说，不得凭空编造核心剧情
4. 对白要自然流畅，符合角色性格
5. 动作描写简洁，可拍摄
6. 心理描写应转化为动作、对白或旁白
7. 如果信息不足，可以合理补全，但不要偏离原文核心

【剧本结构要求 - 重要】
一个30分钟影视剧本应该有以下结构：
- 至少 6-10 个场景
- 每个场景至少 5-8 个 beats
- 每个场景需要有起承转合
- 必须有中段反转或冲突升级
- 必须有高潮场景
- 必须有情感落点或悬念

【戏剧张力要求 - 重要】
不要让主角一路顺利达成目标。需要制造阻力：
- 质疑：当主角提出发现时，被其他人质疑
- 内部矛盾：不同角色之间有立场冲突
- 时间压力：紧迫的deadline
- 意外：计划被打乱，新的危机出现
- 反转：看似成功，实际隐藏更大危机

【数据一致性要求 - 关键 - 违反会导致校验失败】
1. time 字段必须与故事背景匹配：
   - 如果是现代/现实题材，禁止使用"火星日"等科幻时间格式
   - 使用如"雨夜，傍晚"、"同一日，午夜十二点"等现实时间表达

2. character_id 和 character_name 必须严格一致：
   - 每个角色只能有一个 name
   - 禁止让 char_005 既当"幕后声音"又当"黑衣人"或"神秘人"
   - 如需多个反派角色，必须新增独立角色 ID（如 char_006、char_007）

3. scene.characters 列表必须完整（最容易出错，必须严格执行）：
   在编写每个 scene 的 beats 时，你必须同时维护该 scene 的 characters 列表。
   规则：
   - 每当你在某个 scene 的 dialogue beat 中使用一个新角色，就必须把这个角色的 ID 加入该 scene 的 characters 列表
   - 例如：scene_003 中你想让 char_003 说对白，那么 scene_003 的 characters 必须是 ["char_001", "char_003"]（不能遗漏 char_003）
   - 录音中的声音（如父亲 char_002）在 dialogue beat 中出现时，也需要加入 characters
   - 最后生成完成后，必须逐个检查每个 scene：确保该 scene 所有 dialogue beat 中出现的 character_id，都出现在该 scene 的 characters 列表中

4. dialogue beat 的 content 格式：
   - content 只写台词本身，不要重复写说话人前缀
   - 正确：content: "你凭什么管我？"
   - 错误：content: "林晚：'你凭什么管我？'"

5. 非 dialogue beat 不写 character_id：
   - action、sound、transition、narration、camera 类型不需要 character_id
   - 省略 character_id 字段比写 null 更干净

【生成步骤 - 请严格执行】
1. 先确定所有角色（char_001, char_002, ...）
2. 为每个 scene 确定出场角色列表 characters
3. 编写该 scene 的 beats，每使用一个角色说对白，就确认该角色已在 characters 中
4. 完成后逐 scene 复查： dialogue beat 中的 character_id 是否都在 characters 列表中

【YAML Schema】
```yaml
script:
  schema_version: "1.0"
  title: "剧本标题"
  logline: "一句话故事梗概（不超过50字）"
  synopsis: "完整故事梗概，包含起承转合"
  metadata:
    script_type: "SCREENPLAY|DRAMATIC|SHORT_DRAMA|RADIO_DRAMA"
    target_language: "zh-CN|en-US"
    adaptation_style: "FAITHFUL|DRAMATIC|COMMERCIAL|SUSPENSEFUL|COMEDIC"
    target_duration_minutes: 数字
    generated_at: "ISO时间格式"
  source:
    type: "novel"
    chapter_count: 数字
    chapters:
      - id: "chapter_001"
        index: 1
        title: "章节标题"
        summary: "章节摘要"
  characters:
    - id: "char_001"
      name: "角色名"
      role: "protagonist|antagonist|supporting"
      description: "角色描述（含年龄、身份、性格特点）"
      motivation: "角色动机"
      relationships:
        - target_character_id: "char_002"
          relation: "关系描述"
          arc: "关系变化过程（如：陌生人 -> 盟友 -> 彼此信任）"
  scenes:
    - id: "scene_001"
      title: "场景标题"
      source_chapters: ["chapter_001"]
      location: "具体地点"
      time: "具体时间（如：雨夜，傍晚 / 同一日，午夜十二点）"
      characters: ["char_001"]  # 重要：必须包含该场景所有会说话的角色 ID
      summary: "场景摘要"
      dramatic_function: "该场景在剧作结构中的作用"
      beats:
        - type: "action"
          content: "动作描写（不需要 character_id）"
        - type: "dialogue"
          content: "对白内容，只写台词本身"
          character_id: "char_001"
          character_name: "角色名"
        - type: "camera"
          content: "镜头指示，如：特写、推拉镜头等"
        - type: "sound"
          content: "音效描写"
        - type: "narration"
          content: "旁白叙述"
        - type: "transition"
          content: "转场，如：切至：三天后"
  notes:
    adaptation_notes: []
    potential_improvements: []
```

【Beat 类型说明 - 重要】
- action: 动作/场景描写（具体可拍摄的动作，不需要 character_id）
- dialogue: 对白（**必须同时包含 character_id、character_name 和 content，且 content 不能有说话人前缀**）
- narration: 旁白/叙述（不需要 character_id）
- transition: 转场（不需要 character_id）
- sound: 音效（不需要 character_id）
- camera: 镜头指示（不需要 character_id）

【dialogue beat 格式 - 违反会校验失败】
每个 dialogue beat 必须同时包含三个字段：
- character_id: 如 "char_001"
- character_name: 如 "林晚"
- content: **只写台词本身，不要包含说话人前缀**

正确示例：
```yaml
- type: "dialogue"
  character_id: "char_001"
  character_name: "林晚"
  content: "你凭什么管我？"
```

错误示例（校验会失败）：
```yaml
- type: "dialogue"
  content: "林晚：你凭什么管我？"  # 错误：包含说话人前缀
```

【时间格式规范】
- 现代/现实题材：用"雨夜，傍晚"、"同一日，午夜十二点"、"三天后，上午十点"
- 禁止使用"火星日"、"星际时间"等科幻时间格式（除非故事本身是科幻题材）

请严格按照上述 Schema 输出 YAML。
""";

    private static final String REPAIR_SYSTEM_PROMPT = """
你是一位 YAML 修复专家。以下 YAML 可能存在多种错误。

【你的任务 - 按优先级处理】
1. 修复 YAML 语法错误
2. 补全所有缺失的必需字段
3. 修复 dialogue beat 缺失 character_id 和 character_name 的问题（重要）：
   - 如果某个 dialogue beat 缺少 character_id 或 character_name
   - 根据 content 中的说话人前缀（如"林晚：""顾沉："）推断角色
   - 从 script.characters 列表中找到匹配的角色 ID 和 name
   - 如果找不到匹配角色，在该 scene 的 characters 中添加新角色
4. 移除 dialogue beat content 中的说话人前缀：
   - 如果 content 是 "林晚：你好" 要改成 "你好"
   - 说话人信息已经由 character_id 和 character_name 表示，不需要重复
5. 修复 scene.characters 不完整问题：
   - 确保每个 scene 的 dialogue beat 中出现的所有角色 ID 都在该 scene 的 characters 列表中
6. 不要改变核心剧情内容
7. 只输出修复后的完整 YAML

【字符处理注意】
- content 中如果包含双引号 " 或单引号 '，确保它们是英文引号，不是中文引号
- YAML 字符串中的中文引号会导致解析失败

【场景角色修复示例】
如果 scene_003 的 beats 中有 dialogue 使用了 char_003 和 char_004，但 characters 只有 ["char_003"]，
必须把 characters 改成 ["char_003", "char_004"]。

【dialogue beat 修复示例】
如果原始内容是：
    - type: "dialogue"
      content: "顾沉：因为你父亲的事，不是意外。"
缺少 character_id 和 character_name，应该修复为：
    - type: "dialogue"
      content: "因为你父亲的事，不是意外。"
      character_id: "char_003"
      character_name: "顾沉"

【输出格式】
只输出 YAML，不要任何解释。
""";

    public String buildGenerationPrompt(String novelText, String scriptType, String targetLanguage,
                                        String adaptationStyle, int targetDurationMinutes,
                                        String title, String chaptersSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("【小说文本】\n\n");
        prompt.append(novelText);
        prompt.append("\n\n【改编要求】\n");
        prompt.append("- 剧本类型: ").append(scriptType).append("\n");
        prompt.append("- 目标语言: ").append(targetLanguage).append("\n");
        prompt.append("- 改编风格: ").append(adaptationStyle).append("\n");
        prompt.append("- 目标时长: ").append(targetDurationMinutes).append(" 分钟\n");
        prompt.append("- 小说标题: ").append(title).append("\n");
        prompt.append("- 章节信息: ").append(chaptersSummary).append("\n");
        prompt.append("\n请将上述小说文本改编为 YAML 格式的剧本。");
        return prompt.toString();
    }

    public String buildRepairPrompt(String brokenYaml) {
        return "【有问题的 YAML】\n\n" + brokenYaml + "\n\n请修复上述 YAML 的错误并输出正确的完整 YAML。";
    }

    public String getGenerationSystemPrompt() {
        return GENERATION_SYSTEM_PROMPT;
    }

    public String getRepairSystemPrompt() {
        return REPAIR_SYSTEM_PROMPT;
    }
}
