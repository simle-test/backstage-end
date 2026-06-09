package com.example.backstage.service;

import com.example.backstage.dto.ParsedQuestionDTO;

import java.util.List;

public interface AiParseService {

    /**
     * 使用AI解析文档内容，提取题目列表
     * @param content 文档文本内容
     * @return 解析后的题目列表
     */
    List<ParsedQuestionDTO> parseWithAI(String content);
}
