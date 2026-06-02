package com.example.backstage.dto.request;

import java.util.List;

/**
 * 保存资料分析题请求DTO
 */
public class SaveMaterialAnalysisRequest {
    private Integer materialId;
    private String materialTitle;
    private String materialContent;
    private String source;
    private Integer year;
    private List<MaterialProblemRequest> problems;

    // Getters and Setters
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public String getMaterialTitle() { return materialTitle; }
    public void setMaterialTitle(String materialTitle) { this.materialTitle = materialTitle; }
    public String getMaterialContent() { return materialContent; }
    public void setMaterialContent(String materialContent) { this.materialContent = materialContent; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public List<MaterialProblemRequest> getProblems() { return problems; }
    public void setProblems(List<MaterialProblemRequest> problems) { this.problems = problems; }
}
