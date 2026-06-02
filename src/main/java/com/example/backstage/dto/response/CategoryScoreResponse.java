package com.example.backstage.dto.response;

/**
 * 模块得分分布响应
 */
public class CategoryScoreResponse {
    private Double speech;
    private Double logic;
    private Double data;

    public CategoryScoreResponse() {}

    public CategoryScoreResponse(Double speech, Double logic, Double data) {
        this.speech = speech;
        this.logic = logic;
        this.data = data;
    }

    // Getters and Setters
    public Double getSpeech() { return speech; }
    public void setSpeech(Double speech) { this.speech = speech; }
    public Double getLogic() { return logic; }
    public void setLogic(Double logic) { this.logic = logic; }
    public Double getData() { return data; }
    public void setData(Double data) { this.data = data; }
}
