package com.example.backstage.controller;

import com.example.backstage.dto.request.SaveMaterialAnalysisRequest;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.dto.response.MaterialAnalysisDetailResponse;
import com.example.backstage.service.MaterialAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 资料分析题控制器
 */
@RestController
@RequestMapping("/material-analysis")
public class MaterialAnalysisController {

    private final MaterialAnalysisService materialAnalysisService;

    public MaterialAnalysisController(MaterialAnalysisService materialAnalysisService) {
        this.materialAnalysisService = materialAnalysisService;
    }

    /**
     * 获取资料分析题详情（包含材料和所有小题）
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<ApiResponse<MaterialAnalysisDetailResponse>> getMaterialAnalysisDetail(@PathVariable Integer materialId) {
        MaterialAnalysisDetailResponse response = materialAnalysisService.getMaterialAnalysisDetail(materialId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.error("资料分析题不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 保存资料分析题（包含材料和所有小题）
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Void>> saveMaterialAnalysis(@RequestBody SaveMaterialAnalysisRequest request) {
        materialAnalysisService.saveMaterialAnalysis(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
