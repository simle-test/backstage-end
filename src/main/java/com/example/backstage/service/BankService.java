package com.example.backstage.service;

import com.example.backstage.dto.request.CreateBankRequest;
import com.example.backstage.dto.request.UpdateBankRequest;
import com.example.backstage.dto.response.BankDetailResponse;
import com.example.backstage.dto.response.BankResponse;

import java.util.List;

/**
 * 题库服务接口
 */
public interface BankService {

    /**
     * 获取题库列表
     */
    List<BankResponse> getBankList();

    /**
     * 获取题库详情
     */
    BankDetailResponse getBankDetail(Integer id);

    /**
     * 创建题库
     */
    void createBank(CreateBankRequest request);

    /**
     * 更新题库
     */
    void updateBank(Integer id, UpdateBankRequest request);

    /**
     * 删除题库
     */
    void deleteBank(Integer id);
}
