package com.example.backstage.dto;

import java.util.List;

/**
 * 批量导入请求DTO
 */
public class ImportRequestDTO {
    
    /**
     * 题目列表
     */
    private List<ParsedQuestionDTO> questions;
    
    /**
     * 来源名称
     */
    private String source;
    
    public ImportRequestDTO() {
    }
    
    public List<ParsedQuestionDTO> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<ParsedQuestionDTO> questions) {
        this.questions = questions;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}
