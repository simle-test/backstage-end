package com.example.backstage.dto.response;

import java.util.HashMap;
import java.util.Map;

/**
 * 模块得分分布响应
 * 包含6个题库模块的正确率统计
 */
public class CategoryScoreResponse {

    private Double politicalTheory;
    private Double quantityRelation;
    private Double materialAnalysis;
    private Double commonSenseJudgment;
    private Double logicalJudgment;
    private Double languageUnderstanding;

    public CategoryScoreResponse() {}

    public CategoryScoreResponse(Double politicalTheory, Double quantityRelation, Double materialAnalysis,
                                 Double commonSenseJudgment, Double logicalJudgment, Double languageUnderstanding) {
        this.politicalTheory = politicalTheory;
        this.quantityRelation = quantityRelation;
        this.materialAnalysis = materialAnalysis;
        this.commonSenseJudgment = commonSenseJudgment;
        this.logicalJudgment = logicalJudgment;
        this.languageUnderstanding = languageUnderstanding;
    }

    public Double getPoliticalTheory() { return politicalTheory; }
    public void setPoliticalTheory(Double politicalTheory) { this.politicalTheory = politicalTheory; }

    public Double getQuantityRelation() { return quantityRelation; }
    public void setQuantityRelation(Double quantityRelation) { this.quantityRelation = quantityRelation; }

    public Double getMaterialAnalysis() { return materialAnalysis; }
    public void setMaterialAnalysis(Double materialAnalysis) { this.materialAnalysis = materialAnalysis; }

    public Double getCommonSenseJudgment() { return commonSenseJudgment; }
    public void setCommonSenseJudgment(Double commonSenseJudgment) { this.commonSenseJudgment = commonSenseJudgment; }

    public Double getLogicalJudgment() { return logicalJudgment; }
    public void setLogicalJudgment(Double logicalJudgment) { this.logicalJudgment = logicalJudgment; }

    public Double getLanguageUnderstanding() { return languageUnderstanding; }
    public void setLanguageUnderstanding(Double languageUnderstanding) { this.languageUnderstanding = languageUnderstanding; }

    public Map<String, Double> toMap() {
        Map<String, Double> map = new HashMap<>();
        map.put("political_theory", politicalTheory != null ? politicalTheory : 0.0);
        map.put("quantity_relation", quantityRelation != null ? quantityRelation : 0.0);
        map.put("material_analysis", materialAnalysis != null ? materialAnalysis : 0.0);
        map.put("common_sense_judgment", commonSenseJudgment != null ? commonSenseJudgment : 0.0);
        map.put("logical_judgment", logicalJudgment != null ? logicalJudgment : 0.0);
        map.put("language_understanding", languageUnderstanding != null ? languageUnderstanding : 0.0);
        return map;
    }
}