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

    private static final int MAX_CONTENT_LENGTH = 30000;
    private static final int MAX_TOKENS = 12000;
    private static final int CHUNK_SIZE = 8000;

    @Override
    public List<ParsedQuestionDTO> parseWithAI(String content) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        
        if (content.length() <= MAX_CONTENT_LENGTH) {
            // 内容较短，直接解析
            questions = parseSingleChunk(content);
        } else {
            // 内容较长，分段解析
            log.info("文档内容过长({}字符)，开始分段解析", content.length());
            List<String> chunks = splitContentIntoChunks(content);
            
            int totalQuestions = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                log.info("正在解析第 {}/{} 段，长度: {} 字符", i + 1, chunks.size(), chunk.length());
                
                List<ParsedQuestionDTO> chunkQuestions = parseSingleChunk(chunk);
                questions.addAll(chunkQuestions);
                totalQuestions += chunkQuestions.size();
                
                log.info("第 {}/{} 段解析完成，识别 {} 道题目", i + 1, chunks.size(), chunkQuestions.size());
                
                // 段间等待，避免API限流
                if (i < chunks.size() - 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            log.info("分段解析完成，共识别 {} 道题目", totalQuestions);
        }
        
        log.info("AI解析完成，共识别 {} 道题目", questions.size());
        return questions;
    }

    private List<String> splitContentIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        
        // 按题目分隔符分段，尽量在题目边界处分割
        String[] sections = content.split("(?=\\n\\d+[.、])");
        
        StringBuilder currentChunk = new StringBuilder();
        for (String section : sections) {
            if (currentChunk.length() + section.length() > CHUNK_SIZE && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(section);
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }

    private List<ParsedQuestionDTO> parseSingleChunk(String content) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        
        try {
            String requestBody = buildRequestBody(content);
            Request request = new Request.Builder()
                    .url(config.getApiUrl())
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("AI API请求失败: {}", response.code());
                return questions;
            }

            String responseBody = response.body().string();
            questions = parseResponse(responseBody);
            
        } catch (Exception e) {
            log.error("AI解析失败: {}", e.getMessage(), e);
        }
        
        return questions;
    }

    private String buildRequestBody(String content) {
        if (content.length() > MAX_CONTENT_LENGTH) {
            log.warn("单段内容过长，已截断至{}字符", MAX_CONTENT_LENGTH);
            content = content.substring(0, MAX_CONTENT_LENGTH) + "...（内容已截断）";
        }
        
        String prompt = """
            解析题目JSON输出：题型type(single_choice/multiple_choice/true_false/fill_blank)、题干title、选项options数组、分类category、难度difficulty(easy/medium/hard)、答案answer、解析analysis。题目以数字开头，选项A/B/C/D。只输出JSON数组。
            
            文档：
            %s
            
            输出格式：[{"type":"single_choice","title":"...","options":["A.","B.","C.","D."],"category":"","difficulty":"medium","answer":"A","analysis":"..."}]
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
            requestBodyMap.put("max_tokens", MAX_TOKENS);
            
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
                        question.setCorrectAnswer(item.has("answer") ? item.get("answer").asText() : "");
                        question.setAnalysis(item.has("analysis") ? item.get("analysis").asText() : "");
                        
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
