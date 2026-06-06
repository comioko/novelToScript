package com.example.novel2script.service;

import com.example.novel2script.config.AiProperties;
import com.example.novel2script.exception.BusinessException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiClient {

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;

    public AiClient(RestTemplate restTemplate, AiProperties aiProperties) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
    }

    public boolean isConfigured() {
        return aiProperties.isConfigured();
    }

    public String callChatCompletion(String systemPrompt, String userPrompt) {
        if (!aiProperties.isConfigured()) {
            return getMockYamlResponse();
        }

        try {
            String url = aiProperties.getBaseUrl() + "/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(aiProperties.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiProperties.getModel());

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0.4);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null) {
                Map<?, ?> body = response.getBody();
                List<?> choices = (List<?>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    if (message != null) {
                        return message.get("content").toString();
                    }
                }
            }

            throw new BusinessException("AI_RESPONSE_ERROR", "Failed to parse AI response");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("AI_API_ERROR", "AI API call failed: " + e.getMessage());
        }
    }

    private String getMockYamlResponse() {
        return """
script:
  schema_version: "1.0"
  title: "星辰大海的约定"
  logline: "在星际旅行的时代，两个来自不同星球的年轻人在一次意外中相遇，他们必须共同面对宇宙的危机，在质疑与阻力和解中寻找回家的路。"
  synopsis: "2123年，人类已经实现了星际旅行。地球少年林浩在一次火星任务中意外救下了来自阿尔法星的女科学家艾琳。两人发现一颗流浪行星将在九十天后撞击两颗主星。当他们向星际指挥部报告时，却遭到指挥官的质疑——没有人相信一个身份不明的外星人。就在各方争执不下时，新的观测数据显示撞击时间突然提前了二十七天。"
  metadata:
    script_type: "SCREENPLAY"
    target_language: "zh-CN"
    adaptation_style: "DRAMATIC"
    target_duration_minutes: 30
    generated_at: "2026-06-05T10:00:00+08:00"
  source:
    type: "novel"
    chapter_count: 3
    chapters:
      - id: "chapter_001"
        index: 1
        title: "第一章 意外的相遇"
        summary: "林浩在火星基地执行例行巡逻时，救下了因实验事故被困的艾琳。"
      - id: "chapter_002"
        index: 2
        title: "第二章 流浪行星"
        summary: "两人发现流浪行星的轨迹，意识到它将在九十天后撞击两颗主星。"
      - id: "chapter_003"
        index: 3
        title: "第三章 联合行动"
        summary: "林浩和艾琳向指挥部报告，却遭到质疑，最终被迫合作。"
  characters:
    - id: "char_001"
      name: "林浩"
      role: "protagonist"
      description: "23岁，地球星际探索署的年轻学员，聪明勇敢，对宇宙充满好奇。"
      motivation: "证明自己，成为真正的星际探险家"
      relationships:
        - target_character_id: "char_002"
          relation: "从互相试探到并肩作战的伙伴"
          arc: "陌生人 -> 盟友 -> 彼此信任"
    - id: "char_002"
      name: "艾琳"
      role: "protagonist"
      description: "25岁，阿尔法星量子物理学家，冷静理性但内心温暖，背负着沉重秘密。"
      motivation: "拯救自己的文明，不惜一切代价"
      relationships:
        - target_character_id: "char_001"
          relation: "从警惕到信任的战友"
          arc: "怀疑 -> 合作 -> 托付生命"
    - id: "char_003"
      name: "韩睿"
      role: "supporting"
      description: "45岁，火星基地指挥官，老练持重，对未知持怀疑态度。"
      motivation: "维护基地安全，不轻信任何人"
      relationships:
        - target_character_id: "char_001"
          relation: "上下级，对林浩有期待但也有担忧"
        - target_character_id: "char_002"
          relation: "质疑其身份和动机"
    - id: "char_004"
      name: "陈岚"
      role: "supporting"
      description: "38岁，星际高级联络官，负责地球与阿尔法星的沟通协调。"
      motivation: "维护两个文明的和平关系"
      relationships:
        - target_character_id: "char_002"
          relation: "同来自阿尔法星，但立场不同"
        - target_character_id: "char_003"
          relation: "工作伙伴，有时意见相左"
  scenes:
    - id: "scene_001"
      title: "火星巡逻"
      source_chapters:
        - "chapter_001"
      location: "火星基地外围·东北区"
      time: "火星日第184日，午后"
      characters:
        - "char_001"
      summary: "林浩在例行巡逻中发现异常信号源。"
      dramatic_function: "建立主角，展示其专业能力和好奇心"
      beats:
        - type: "action"
          content: "火星地表，黄沙漫天。林浩驾驶小型巡逻车在岩石群间穿行，远处是火星基地的穹顶。"
        - type: "camera"
          content: "镜头从空中俯拍，巡逻车的身影在荒漠中显得微不足道。"
        - type: "action"
          content: "巡逻车仪表盘上的信号探测器突然亮起红点。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "异常信号？这一带没有采矿点...坐标是..."
        - type: "sound"
          content: "通讯器里传来断断续续的杂音，像是什么东西在干扰。"
        - type: "action"
          content: "林浩关闭引擎，向信号源方向走去。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "顾台，收到请回复。我是林浩，在东北区发现异常信号。"
        - type: "transition"
          content: "切至：火星基地主控室。"
    - id: "scene_002"
      title: "坠落点"
      source_chapters:
        - "chapter_001"
      location: "火星基地外围·信号源坐标"
      time: "同一时段"
      characters:
        - "char_001"
        - "char_002"
      summary: "林浩发现坠毁的飞行器残骸和受困的艾琳。"
      dramatic_function: "引入第二主角，制造悬念"
      beats:
        - type: "action"
          content: "林浩穿过一片黑色岩石，发现一艘小型飞行器半埋在沙中，外壳烧焦，冒着青烟。"
        - type: "sound"
          content: "金属冷却的嗤嗤声，混合着风沙的呼啸。"
        - type: "camera"
          content: "镜头推向飞行器残骸，焦距逐渐拉近，一道微弱的光从裂缝中透出。"
        - type: "action"
          content: "林浩快步上前，透过裂缝看见一个身穿奇异制服的女子被困在驾驶舱内。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "有人吗？你能听到我吗？"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "(虚弱地) ...你是谁？这里是..."
        - type: "action"
          content: "林浩试图打开舱门，但舱体已经变形挤压。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "坚持住！我去拿工具！"
        - type: "transition"
          content: "切至：十分钟后，林浩带着切割设备返回。"
    - id: "scene_003"
      title: "医疗室"
      source_chapters:
        - "chapter_001"
        - "chapter_002"
      location: "火星基地·医疗室"
      time: "当日傍晚"
      characters:
        - "char_001"
        - "char_002"
        - "char_003"
      summary: "艾琳醒来，向林浩和韩睿说明流浪行星的威胁。"
      dramatic_function: "揭示核心冲突，建立主要矛盾"
      beats:
        - type: "action"
          content: "医疗室内，艾琳躺在病床上缓缓睁开眼睛。林浩站在床边，韩睿指挥官双手抱胸站在门口。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "这是...地球的火星基地？"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "是的，你漂流到了这里。能告诉我们你是谁吗？"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我叫艾琳，来自阿尔法星。我...我的任务是警告你们。"
        - type: "action"
          content: "韩睿走近几步，眉头紧锁。"
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "阿尔法星？警告什么？"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "一颗流浪行星。代号'克洛诺斯'。它会在九十天后撞击..."
        - type: "action"
          content: "艾琳突然坐起身，抓住林浩的手臂。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "不，不对。我计算过轨道——它不只是威胁阿尔法星。地球也在撞击范围内。"
        - type: "camera"
          content: "镜头定格在韩睿的脸上，他的表情从怀疑变成了凝重。"
        - type: "transition"
          content: "切至：火星基地指挥室，巨大的星图投影悬浮在空中。"
    - id: "scene_004"
      title: "第一次警告"
      source_chapters:
        - "chapter_002"
      location: "火星基地·指挥室"
      time: "同日夜晚"
      characters:
        - "char_001"
        - "char_002"
        - "char_003"
        - "char_004"
      summary: "艾琳展示数据，却遭到质疑。林浩试图说服韩睿。"
      dramatic_function: "制造阻力，让主角的发现无法立刻被相信"
      beats:
        - type: "action"
          content: "巨大的星图投影悬浮在指挥室中央，一条红色轨迹穿过两颗主星的轨道，克洛诺斯的模拟影像正在缓慢移动。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "这是阿尔法星天文台的观测数据。克洛诺斯的质量是地球的千分之一，但它的速度..."
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "停一下。你要我们相信一个身份未确认的外星科学家？"
        - type: "action"
          content: "艾琳的手指在投影上滑动，调出一组数据。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我不是来请求信任的。我是来警告你们。如果地球不采取措施，九十天后，两颗文明都将毁灭。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "指挥官，我亲眼看见她的飞行器。那不是伪装，是真实的坠毁。"
        - type: "dialogue"
          character_id: "char_004"
          character_name: "陈岚"
          content: "韩指挥官，如果数据是真的，这将是我们两个文明面临的最大威胁。"
        - type: "sound"
          content: "警报声突然响起，星图上的红色轨迹开始急促闪烁。"
        - type: "action"
          content: "一名操作员惊恐地站起来。"
        - type: "dialogue"
          character_id: "char_003"
          character_name: "操作员"
          content: "指挥官！观测数据更新了——撞击时间提前了二十七天！"
        - type: "camera"
          content: "镜头缓缓推向韩睿的脸，汗珠从额头滑落。"
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "......我们需要重新评估。"
        - type: "transition"
          content: "切至：基地外，通讯塔在火星风暴中旋转。"
    - id: "scene_005"
      title: "被迫合作"
      source_chapters:
        - "chapter_002"
        - "chapter_003"
      location: "火星基地·会议室"
      time: "次日清晨"
      characters:
        - "char_001"
        - "char_002"
        - "char_003"
        - "char_004"
      summary: "时间压力下，各方被迫开始合作，但仍有内部阻力。"
      dramatic_function: "展示合作的必要性，同时埋下内部冲突的种子"
      beats:
        - type: "action"
          content: "简陋的会议室内，四人围坐在一张金属桌旁。窗外，火星的地平线呈现出铁锈色的微光。"
        - type: "dialogue"
          character_id: "char_004"
          character_name: "陈岚"
          content: "阿尔法星高层已经同意了——如果我们能提供地球的联合防御方案，他们会共享全部轨道数据。"
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "但问题是，我们的技术无法在六十三天内单独完成这个规模的防御工程。"
        - type: "action"
          content: "艾琳站起身，手指在桌上的简易投影仪上滑动。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "除非我们两国联合起来，共享资源和坐标。否则..."
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "否则？"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "否则阿尔法星会撤离部分人口到更远的殖民卫星。地球...只能靠自己。"
        - type: "action"
          content: "沉默。林浩看向韩睿，又看向艾琳。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "指挥官，如果我们不联合，地球就完了。"
        - type: "dialogue"
          character_id: "char_003"
          character_name: "韩睿"
          content: "(长久沉默后) ...好。我们联合。但林浩，你负责监督整个项目——如果你发现任何问题，立刻向我报告。"
        - type: "camera"
          content: "镜头定格在艾琳脸上，她的眼神里闪过一丝复杂的光芒。"
        - type: "transition"
          content: "切至：联合行动启动仪式。"
    - id: "scene_006"
      title: "联合行动"
      source_chapters:
        - "chapter_003"
      location: "地球轨道中转站"
      time: "两周后"
      characters:
        - "char_001"
        - "char_002"
        - "char_003"
        - "char_004"
      summary: "联合行动启动，但新的危机正在酝酿。"
      dramatic_function: "高潮与新的悬念，为后续故事埋下伏笔"
      beats:
        - type: "action"
          content: "地球轨道中转站的舷窗外，两艘飞船正在对接——一艘来自地球，一艘来自阿尔法星。漆黑的宇宙中，星光点点。"
        - type: "sound"
          content: "对接舱门打开的气压声，混合着远处引擎的低鸣。"
        - type: "camera"
          content: "镜头从舷窗缓缓拉近，穿过走廊，最终定格在一张简陋的办公桌前。"
        - type: "action"
          content: "林浩和艾琳站在桌前，桌上铺满了轨道数据和工程图纸。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "防御盾发生器的理论模型完成了，但要实际建造还需要阿尔法星的核心材料。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "材料已经在运输途中了。但林浩，有件事我一直没告诉你。"
        - type: "action"
          content: "艾琳犹豫了一下，看向舷窗外的星空。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我的任务不只是警告。我来之前，阿尔法星高层内部有过争论——有人主张只撤离，不救援地球。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "什么？"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我是少数派。我选择来警告你们，是因为...我不同意牺牲任何一方。"
        - type: "camera"
          content: "镜头推向林浩的脸，他的表情从震惊逐渐变为坚定。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "那就让他们看看，两个文明可以联合起来。"
        - type: "action"
          content: "两人相视。远处，地球的蓝色弧线在星空中熠熠发光。"
        - type: "transition"
          content: "淡出。字幕升起：'六十三天后'。"
  notes:
    adaptation_notes:
      - "将小说中的心理描写转化为动作和对白。"
      - "增加戏剧冲突：韩睿的质疑、流浪行星轨迹提前、艾琳的秘密。"
      - "保留科幻氛围：星图、轨道计算、飞船对接等视觉元素。"
    potential_improvements:
      - "可以深化林浩和艾琳之间的情感线。"
      - "可以增加更多内部阻力的角色，如反对合作的军方人物。"
      - "可以设计一个反派角色，例如故意拖延进度的承包商。"
""";
    }
}
