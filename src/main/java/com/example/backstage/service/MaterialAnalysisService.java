package com.example.backstage.service;

import com.example.backstage.dto.request.SaveMaterialAnalysisRequest;
import com.example.backstage.dto.response.MaterialAnalysisDetailResponse;

/**
 * 资料分析题服务接口
 */
public interface MaterialAnalysisService {

    /**
     * 根据材料ID获取资料分析题详情（包含材料和所有小题）
     */
    MaterialAnalysisDetailResponse getMaterialAnalysisDetail(Integer materialId);

    /**
     * 保存资料分析题（包含材料和所有小题）
     */
    void saveMaterialAnalysis(SaveMaterialAnalysisRequest request);
}
