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
  title: "【MOCK】星辰大海的约定"
  logline: "在星际旅行的时代，两个来自不同星球的年轻人在一次意外中相遇，他们必须共同面对宇宙的危机，寻找回家的路。"
  synopsis: "2123年，人类已经实现了星际旅行。地球少年林浩在一次火星任务中意外救下了来自阿尔法星的女科学家艾琳。两人发现彼此的星球都面临同样的威胁——一颗失控的流浪行星即将毁灭两个文明。在星际外交官的帮助下，他们踏上了说服两个文明联合抗敌的旅程。"
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
        summary: "两人发现流浪行星的轨迹，意识到它将在三个月内撞击两颗主星。"
      - id: "chapter_003"
        index: 3
        title: "第三章 联合行动"
        summary: "林浩和艾琳决定联合两个文明，共同应对宇宙危机。"
  characters:
    - id: "char_001"
      name: "林浩"
      role: "protagonist"
      description: "23岁，地球星际探索署的年轻学员，聪明勇敢，对宇宙充满好奇。"
      motivation: "证明自己，成为真正的星际探险家"
      relationships:
        - target_character_id: "char_002"
          relation: "朋友/战友"
    - id: "char_002"
      name: "艾琳"
      role: "protagonist"
      description: "25岁，阿尔法星最年轻的量子物理学家，冷静理性但内心温暖。"
      motivation: "拯救自己的文明，保护所爱的人"
      relationships:
        - target_character_id: "char_001"
          relation: "朋友/战友"
  scenes:
    - id: "scene_001"
      title: "火星基地外"
      source_chapters:
        - "chapter_001"
      location: "火星基地外围"
      time: "白天"
      characters:
        - "char_001"
      summary: "林浩在例行巡逻中发现受困的艾琳。"
      dramatic_function: "建立主角，引入核心冲突"
      beats:
        - type: "action"
          content: "火星基地外围，黄沙漫天。林浩穿着宇航服在检查一个废弃的采矿探测器。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "这里也没有任何信号...等等，那个方向是什么？"
        - type: "action"
          content: "林浩发现远处有微弱的闪光，他向那个方向跑去。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "喂！有人吗？"
        - type: "action"
          content: "林浩发现艾琳被压在倒塌的实验设备下。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "坚持住！我来帮你！"
    - id: "scene_002"
      title: "火星基地内部"
      source_chapters:
        - "chapter_001"
      location: "火星基地医疗室"
      time: "稍后"
      characters:
        - "char_001"
        - "char_002"
      summary: "艾琳醒来，与林浩交谈，两人发现共同面临的问题。"
      dramatic_function: "建立人物关系，埋下冲突种子"
      beats:
        - type: "action"
          content: "医疗室内，艾琳慢慢睁开眼睛。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "这是...哪里？"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "你醒了？这里是火星基地，我是林浩。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我的实验...失败了...不，这不只是普通的实验事故..."
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "冷静一下，慢慢说。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "我在监测流浪行星的轨迹...它...它会撞击阿尔法星！"
    - id: "scene_003"
      title: "星际会议"
      source_chapters:
        - "chapter_002"
        - "chapter_003"
      location: "星际联合指挥中心"
      time: "数周后"
      characters:
        - "char_001"
        - "char_002"
      summary: "林浩和艾琳向两个文明的首领汇报流浪行星的威胁。"
      dramatic_function: "情节高潮，各方力量汇聚"
      beats:
        - type: "action"
          content: "星际指挥中心大厅，各方代表齐聚一堂。"
        - type: "dialogue"
          character_id: "char_002"
          character_name: "艾琳"
          content: "根据我的计算，流浪行星将在九十天后到达阿尔法星轨道。"
        - type: "dialogue"
          character_id: "char_001"
          character_name: "林浩"
          content: "同样的行星轨道也会影响地球。两颗星球实际上是唇亡齿寒的关系。"
        - type: "action"
          content: "全场陷入沉默。"
        - type: "transition"
          content: "切至：星际联合行动启动仪式。"
  notes:
    adaptation_notes:
      - "将小说中的心理描写转化为动作和对白。"
      - "增加戏剧冲突，强调两人的不同背景。"
    potential_improvements:
      - "后续可以增加更多动作场面。"
      - "可以深化两人之间的情感线。"
""";
    }
}
