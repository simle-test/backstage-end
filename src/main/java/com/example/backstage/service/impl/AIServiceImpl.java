package com.example.backstage.service.impl;

import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {

    private static final Logger log = LoggerFactory.getLogger(AIServiceImpl.class);

    @Override
    public String generateAnswer(ParsedQuestionDTO question) {
        // TODO: 集成真实的AI接口（如OpenAI、文心一言等）
        // 这里先返回一个占位符，后续替换为真实的AI调用

        String type = question.getType();
        List<String> options = question.getOptions();

        switch (type) {
            case "single_choice":
                // 单选题：随机选择一个选项
                if (options != null && !options.isEmpty()) {
                    int index = (int) (Math.random() * options.size());
                    return String.valueOf((char) ('A' + index));
                }
                return "A";
            case "multiple_choice":
                // 多选题：随机选择2-3个选项
                if (options != null && !options.isEmpty()) {
                    int count = Math.min(options.size(), 2 + (int) (Math.random() * 2));
                    StringBuilder answer = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        if (answer.length() > 0) {
                            answer.append(",");
                        }
                        answer.append((char) ('A' + i));
                    }
                    return answer.toString();
                }
                return "A,B";
            case "true_false":
                // 判断题：随机选择对或错
                return Math.random() > 0.5 ? "正确" : "错误";
            case "fill_blank":
                // 填空题：返回占位符
                return "答案待补充";
            case "essay":
                // 解答题：返回占位符
                return "答案待补充";
            default:
                return "A";
        }
    }

    @Override
    public String generateAnalysis(ParsedQuestionDTO question) {
        // TODO: 集成真实的AI接口生成解析
        // 这里先返回一个占位符

        String type = question.getType();
        switch (type) {
            case "single_choice":
            case "multiple_choice":
                return "解析待补充";
            case "true_false":
                return "解析待补充";
            case "fill_blank":
                return "解析待补充";
            case "essay":
                return "解析待补充";
            default:
                return "解析待补充";
        }
    }

    @Override
    public void generateAnswersAndAnalysis(List<ParsedQuestionDTO> questions) {
        for (ParsedQuestionDTO question : questions) {
            try {
                String answer = generateAnswer(question);
                String analysis = generateAnalysis(question);

                question.setCorrectAnswer(answer);
                question.setAnalysis(analysis);

                log.info("为题目 {} 生成答案和解析成功", question.getOrderNum());
            } catch (Exception e) {
                log.error("为题目 {} 生成答案和解析失败", question.getOrderNum(), e);
            }
        }
    }
}