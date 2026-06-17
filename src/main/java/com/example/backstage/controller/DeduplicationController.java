package com.example.backstage.controller;

import com.example.backstage.dto.DeduplicationRequest;
import com.example.backstage.dto.DeduplicationResult;
import com.example.backstage.dto.DuplicateDetail;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.service.DeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deduplication")
public class DeduplicationController {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationController.class);
    
    private final DeduplicationService deduplicationService;

    public DeduplicationController(DeduplicationService deduplicationService) {
        this.deduplicationService = deduplicationService;
    }

    @PostMapping("/detect")
    public ResponseEntity<ApiResponse<DeduplicationResult>> detectDuplicates(
            @RequestBody DeduplicationRequest request) {
        try {
            log.info("检测重复请求: mode={}, fields={}", request.getMode(), request.getFields());
            DeduplicationResult result = deduplicationService.detectDuplicates(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("检测重复失败", e);
            return ResponseEntity.ok(ApiResponse.error("检测失败: " + e.getMessage()));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<ApiResponse<DeduplicationResult>> removeDuplicates(
            @RequestBody DeduplicationRequest request) {
        try {
            log.info("移除重复请求: mode={}, fields={}", request.getMode(), request.getFields());
            DeduplicationResult result = deduplicationService.removeDuplicates(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("移除重复失败", e);
            return ResponseEntity.ok(ApiResponse.error("移除失败: " + e.getMessage()));
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<DeduplicationResult>> previewDeduplication(
            @RequestBody DeduplicationRequest request) {
        try {
            log.info("预览去重请求");
            DeduplicationResult result = deduplicationService.previewDeduplication(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("预览去重失败", e);
            return ResponseEntity.ok(ApiResponse.error("预览失败: " + e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> countDuplicates() {
        try {
            long count = deduplicationService.countDuplicates();
            Map<String, Object> result = new HashMap<>();
            result.put("duplicateCount", count);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("统计重复失败", e);
            return ResponseEntity.ok(ApiResponse.error("统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DuplicateDetail>>> findDuplicatesByField(
            @RequestParam("fieldName") String fieldName,
            @RequestParam("fieldValue") String fieldValue) {
        try {
            log.info("按字段搜索重复: fieldName={}, fieldValue={}", fieldName, fieldValue);
            List<DuplicateDetail> duplicates = deduplicationService.findDuplicatesByField(fieldName, fieldValue);
            return ResponseEntity.ok(ApiResponse.success(duplicates));
        } catch (Exception e) {
            log.error("按字段搜索重复失败", e);
            return ResponseEntity.ok(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    @PostMapping("/quick-detect")
    public ResponseEntity<ApiResponse<DeduplicationResult>> quickDetect(
            @RequestParam(value = "mode", defaultValue = "exact") String mode,
            @RequestParam(value = "category", required = false) String category) {
        try {
            DeduplicationRequest request = new DeduplicationRequest();
            request.setMode(mode);
            request.setFields(List.of("title"));
            request.setCategoryFilter(category);
            
            log.info("快速检测重复: mode={}, category={}", mode, category);
            DeduplicationResult result = deduplicationService.detectDuplicates(request);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("快速检测失败", e);
            return ResponseEntity.ok(ApiResponse.error("检测失败: " + e.getMessage()));
        }
    }
}