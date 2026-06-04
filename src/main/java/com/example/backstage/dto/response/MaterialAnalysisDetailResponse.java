package com.example.backstage.dto.response;

import java.util.List;

/**
 * 资料分析题详情响应
 * 包含材料内容和关联的所有小题
 */
public class MaterialAnalysisDetailResponse {
    private Integer materialId;
    private String materialTitle;
    private String materialContent;
    private String source;
    private Integer year;
    private String categoryId;
    private String categoryName;
    private String imageUrl;
    private List<MaterialProblemItem> problems;
    private Integer problemCount;

    public MaterialAnalysisDetailResponse() {}

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
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<MaterialProblemItem> getProblems() { return problems; }
    public void setProblems(List<MaterialProblemItem> problems) { 
        this.problems = problems; 
        this.problemCount = problems != null ? problems.size() : 0;
    }
    public Integer getProblemCount() { return problemCount; }
    public void setProblemCount(Integer problemCount) { this.problemCount = problemCount; }
}
