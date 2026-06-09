package com.example.backstage.controller;

import com.example.backstage.dto.request.CreateBankRequest;
import com.example.backstage.dto.request.UpdateBankRequest;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.dto.response.BankDetailResponse;
import com.example.backstage.dto.response.BankResponse;
import com.example.backstage.service.BankService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题库管理控制器
 */
@RestController
@RequestMapping("/banks")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    /**
     * 获取题库列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BankResponse>>> getBankList() {
        List<BankResponse> response = bankService.getBankList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取题库详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankDetailResponse>> getBankDetail(@PathVariable Integer id) {
        BankDetailResponse response = bankService.getBankDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 创建题库
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createBank(@RequestBody CreateBankRequest request) {
        bankService.createBank(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功"));
    }

    /**
     * 更新题库
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateBank(
            @PathVariable Integer id,
            @RequestBody UpdateBankRequest request) {
        bankService.updateBank(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功"));
    }

    /**
     * 删除题库
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBank(@PathVariable Integer id) {
        bankService.deleteBank(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
