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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        // 限制内容长度，避免token消耗过大（大约限制在2000字符以内）
        if (content.length() > 2000) {
            log.warn("文档内容过长，已截断至2000字符，可能影响解析效果");
            content = content.substring(0, 2000) + "...（内容已截断）";
        }
        
        // 使用更简洁的Prompt，减少输入token
        String prompt = """
            解析题目JSON输出：题型type(single_choice/multiple_choice/true_false/fill_blank)、题干title、选项options数组、分类category、难度difficulty(easy/medium/hard)。题目以数字开头，选项A/B/C/D。只输出JSON数组。
            
            文档：
            %s
            
            输出格式：[{"type":"single_choice","title":"...","options":["A.","B.","C.","D."],"category":"","difficulty":"medium"}]
            """.formatted(content);

        try {
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", "deepseek-chat");
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBodyMap.put("messages", messages);
            
            requestBodyMap.put("temperature", 0.1);
            requestBodyMap.put("max_tokens", 2000);
            
            return objectMapper.writeValueAsString(requestBodyMap);
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
