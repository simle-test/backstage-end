package com.example.backstage.dto;

public class QuestionImportRequest {
    private String category; // 题目分类
    private String bankId; // 题库ID（可选）
    private String dedup; // 去重方式：skip, override, all
    private boolean autoDetect; // 是否自动识别题型
    private boolean keepFormat; // 是否保留原格式

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getDedup() {
        return dedup;
    }

    public void setDedup(String dedup) {
        this.dedup = dedup;
    }

    public boolean isAutoDetect() {
        return autoDetect;
    }

    public void setAutoDetect(boolean autoDetect) {
        this.autoDetect = autoDetect;
    }

    public boolean isKeepFormat() {
        return keepFormat;
    }

    public void setKeepFormat(boolean keepFormat) {
        this.keepFormat = keepFormat;
    }
}