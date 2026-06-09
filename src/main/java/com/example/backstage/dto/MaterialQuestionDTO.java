package com.example.backstage.dto;

import java.util.List;

/**
 * 资料分析大题DTO - 包含材料和多个子题目
 */
public class MaterialQuestionDTO {
    private String material; // 材料内容（大题干）
    private List<ParsedQuestionDTO> subQuestions; // 子题目列表
    private String category; // 分类
    private Integer orderNum; // 大题序号

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public List<ParsedQuestionDTO> getSubQuestions() {
        return subQuestions;
    }

    public void setSubQuestions(List<ParsedQuestionDTO> subQuestions) {
        this.subQuestions = subQuestions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}