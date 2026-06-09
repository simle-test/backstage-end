package com.example.backstage.service.impl;

import com.example.backstage.config.DeepSeekConfig;
import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.service.AiParseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class AiParseServiceImpl implements AiParseService {

    private static final Logger log = LoggerFactory.getLogger(AiParseServiceImpl.class);

    private final OkHttpClient httpClient;
    private final DeepSeekConfig config;
    private final ObjectMapper objectMapper;

    public AiParseServiceImpl(OkHttpClient httpClient, DeepSeekConfig config) {
        this.httpClient = httpClient;
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<ParsedQuestionDTO> parseWithAI(String content) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        
        try {
            // 构建请求
            String requestBody = buildRequestBody(content);
            Request request = new Request.Builder()
                    .url(config.getApiUrl())
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            // 发送请求
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("AI API请求失败: {}", response.code());
                return questions;
            }

            // 解析响应
            String responseBody = response.body().string();
            questions = parseResponse(responseBody);
            
        } catch (Exception e) {
            log.error("AI解析失败: {}", e.getMessage(), e);
        }
        
        log.info("AI解析完成，共识别 {} 道题目", questions.size());
        return questions;
    }

    private String buildRequestBody(String content) {
        // 构建精心设计的Prompt，引导AI正确解析题目
        String prompt = """
            请帮我解析以下文档中的题目，按照JSON格式输出。
            
            文档内容：
            ---
            %s
            ---
            
            请识别文档中的所有题目，每个题目包含：
            1. type: 题型（单选题 single_choice，多选题 multiple_choice，判断题 true_false，填空题 fill_blank）
            2. title: 题目内容（只包含题干，不包含选项）
            3. options: 选项列表（格式如 ["A. 选项内容", "B. 选项内容"]）
            4. category: 题目分类（如果文档中有分类标题如"第一部分 资料分析"，请提取）
            5. difficulty: 难度（easy简单, medium中等, hard困难）
            
            注意事项：
            - 如果文档开头有材料数据（如统计数据、文章段落），请跳过，只提取真正的题目
            - 题目通常以数字开头（如"1."、"2."），后面跟着选项（A、B、C、D）
            - 如果没有明确的分类，category字段可以为空字符串
            - 只输出JSON数组，不要包含其他文字
            
            输出格式示例：
            [
              {"type": "single_choice", "title": "题目内容", "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"], "category": "资料分析", "difficulty": "medium"},
              {"type": "multiple_choice", "title": "多选题内容", "options": ["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"], "category": "", "difficulty": "hard"}
            ]
            """.formatted(content);

        try {
            return objectMapper.writeValueAsString(new Object() {
                public final String model = "deepseek-chat";
                public final Object[] messages = {
                    new Object() {
                        public final String role = "user";
                        public final String content = prompt;
                    }
                };
                public final double temperature = 0.1;
                public final int max_tokens = 4000;
            });
        } catch (Exception e) {
            log.error("构建请求体失败", e);
            return "{}";
        }
    }

    private List<ParsedQuestionDTO> parseResponse(String responseBody) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                String content = choices.get(0).get("message").get("content").asText();
                
                // 清理可能的markdown代码块标记
                content = content.replaceAll("^```json\\s*", "").replaceAll("\\s*```$", "").trim();
                
                // 解析JSON数组
                JsonNode arrayNode = objectMapper.readTree(content);
                if (arrayNode.isArray()) {
                    int orderNum = 1;
                    Iterator<JsonNode> elements = arrayNode.elements();
                    while (elements.hasNext()) {
                        JsonNode item = elements.next();
                        ParsedQuestionDTO question = new ParsedQuestionDTO();
                        question.setOrderNum(orderNum++);
                        question.setType(item.has("type") ? item.get("type").asText() : "single_choice");
                        question.setTitle(item.has("title") ? item.get("title").asText() : "");
                        question.setCategory(item.has("category") ? item.get("category").asText() : "");
                        question.setDifficulty(item.has("difficulty") ? item.get("difficulty").asText() : "medium");
                        
                        // 解析选项
                        List<String> options = new ArrayList<>();
                        if (item.has("options") && item.get("options").isArray()) {
                            Iterator<JsonNode> optElements = item.get("options").elements();
                            while (optElements.hasNext()) {
                                options.add(optElements.next().asText());
                            }
                        }
                        question.setOptions(options);
                        
                        questions.add(question);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage(), e);
        }
        
        return questions;
    }
}
