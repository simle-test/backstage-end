package com.example.backstage.service;

import com.example.backstage.dto.ParsedQuestionDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档解析服务接口
 */
public interface DocumentParserService {
    
    /**
     * 解析文档并提取题目
     * @param file 文档文件
     * @param category 题目分类
     * @return 解析后的题目列表
     */
    List<ParsedQuestionDTO> parseDocument(MultipartFile file, String category);
    
    /**
     * 识别题目类型
     * @param content 题目内容
     * @return 题目类型
     */
    String detectQuestionType(String content);
    
    /**
     * 提取题干和选项
     * @param content 题目内容
     * @return 包含题干和选项的数组
     */
    Object[] extractTitleAndOptions(String content);
}
