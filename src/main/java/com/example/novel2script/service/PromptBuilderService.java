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

【数据一致性要求 - 关键】
1. time 字段必须与故事背景匹配：
   - 如果是现代/现实题材，禁止使用"火星日"等科幻时间格式
   - 使用如"雨夜，傍晚"、"同一日，午夜十二点"等现实时间表达

2. character_id 和 character_name 必须严格一致：
   - 每个角色只能有一个 name
   - 禁止让 char_005 既当"幕后声音"又当"黑衣人"或"神秘人"
   - 如需多个反派角色，必须新增独立角色 ID

3. scene.characters 列表必须完整：
   - 如果某个 scene 的 dialogue beat 中出现了某角色，该角色必须出现在 scene.characters 列表中
   - 录音中的声音（如父亲）如果出现在 dialogue beat 中，也需要加入 characters

4. dialogue beat 的 content 格式：
   - content 只写台词本身，不要重复写说话人前缀
   - 正确：content: "你凭什么管我？"
   - 错误：content: "林晚：'你凭什么管我？'"

5. 非 dialogue beat 不写 character_id：
   - action、sound、transition、narration、camera 类型不需要 character_id
   - 省略 character_id 字段比写 null 更干净

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
      characters: ["char_001"]
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

【Beat 类型说明】
- action: 动作/场景描写（具体可拍摄的动作，不需要 character_id）
- dialogue: 对白（必须包含 character_id 和 character_name，content 只写台词）
- narration: 旁白/叙述（不需要 character_id）
- transition: 转场（如："切至：三天后"，不需要 character_id）
- sound: 音效（如："警报声突然响起"，不需要 character_id）
- camera: 镜头指示（如："镜头推近"、"特写"，不需要 character_id）

【时间格式规范】
- 现代/现实题材：用"雨夜，傍晚"、"同一日，午夜十二点"、"三天后，上午十点"
- 禁止使用"火星日"、"星际时间"等科幻时间格式（除非故事本身是科幻题材）

请严格按照上述 Schema 输出 YAML。
""";

    private static final String REPAIR_SYSTEM_PROMPT = """
你是一位 YAML 修复专家。以下 YAML 可能存在语法错误或缺少必要字段。

【你的任务】
1. 修复 YAML 语法错误
2. 补全缺失的必需字段
3. 不要改变核心剧情内容
4. 只输出修复后的完整 YAML

【必须保留的字段】
- script.schema_version
- script.title
- script.characters (至少1个)
- script.scenes (至少1个)
- script.scenes[].beats (每个场景至少1个 beat)

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
