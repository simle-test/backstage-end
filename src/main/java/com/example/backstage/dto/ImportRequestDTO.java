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
    
    /**
     * 是否启用去重功能（默认开启）
     */
    private Boolean enableDeduplication = true;
    
    /**
     * 去重模式：exact（精确匹配）或 similar（模糊匹配）
     */
    private String deduplicationMode = "exact";
    
    /**
     * 导入策略：skip（跳过重复）、override（覆盖重复）、all（全部导入）
     */
    private String importStrategy = "skip";
    
    /**
     * 模糊匹配时的相似度阈值（0.0-1.0）
     */
    private Double similarityThreshold = 0.9;
    
    /**
     * 去重字段，默认按题目标题去重
     */
    private List<String> deduplicationFields = List.of("title");
    
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
    
    public Boolean getEnableDeduplication() {
        return enableDeduplication;
    }
    
    public void setEnableDeduplication(Boolean enableDeduplication) {
        this.enableDeduplication = enableDeduplication;
    }
    
    public String getDeduplicationMode() {
        return deduplicationMode;
    }
    
    public void setDeduplicationMode(String deduplicationMode) {
        this.deduplicationMode = deduplicationMode;
    }
    
    public String getImportStrategy() {
        return importStrategy;
    }
    
    public void setImportStrategy(String importStrategy) {
        this.importStrategy = importStrategy;
    }
    
    public Double getSimilarityThreshold() {
        return similarityThreshold;
    }
    
    public void setSimilarityThreshold(Double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
    
    public List<String> getDeduplicationFields() {
        return deduplicationFields;
    }
    
    public void setDeduplicationFields(List<String> deduplicationFields) {
        this.deduplicationFields = deduplicationFields;
    }
}
