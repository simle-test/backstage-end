package com.example.backstage.service.impl;

import com.example.backstage.dto.request.CreateBankRequest;
import com.example.backstage.dto.request.UpdateBankRequest;
import com.example.backstage.dto.response.BankDetailResponse;
import com.example.backstage.dto.response.BankResponse;
import com.example.backstage.entity.Bank;
import com.example.backstage.repository.BankRepository;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.service.BankService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 题库服务实现
 */
@Service
public class BankServiceImpl implements BankService {

    private static final Logger log = LoggerFactory.getLogger(BankServiceImpl.class);

    private final BankRepository bankRepository;
    private final QuestionRepository questionRepository;

    public BankServiceImpl(BankRepository bankRepository, QuestionRepository questionRepository) {
        this.bankRepository = bankRepository;
        this.questionRepository = questionRepository;
    }
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<BankResponse> getBankList() {
        List<Bank> banks = bankRepository.findAll();
        List<BankResponse> responses = new ArrayList<>();
        
        for (Bank bank : banks) {
            String categoryId = getCategoryIdByBankName(bank.getName());
            long count = categoryId != null ? questionRepository.countByCategoryIdContaining(categoryId) : 0;
            
            responses.add(new BankResponse(
                bank.getId(),
                bank.getName(),
                bank.getDesc(),
                bank.getColor(),
                count,
                bank.getUpdatedAt() != null ? bank.getUpdatedAt().format(FORMATTER) : ""
            ));
        }
        
        return responses;
    }
    
    private String getCategoryIdByBankName(String bankName) {
        if (bankName == null) return null;
        
        if (bankName.contains("政治理论")) return "political_theory";
        if (bankName.contains("数量关系")) return "quantity_relation";
        if (bankName.contains("资料分析")) return "material_analysis";
        if (bankName.contains("常识判断")) return "common_sense_judgment";
        if (bankName.contains("判断推理")) return "logical_judgment";
        if (bankName.contains("言语理解")) return "language_understanding";
        
        return null;
    }

    @Override
    public BankDetailResponse getBankDetail(Integer id) {
        Bank bank = bankRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("题库不存在"));
        
        return new BankDetailResponse(
            bank.getId(),
            bank.getName(),
            bank.getDesc(),
            bank.getColor(),
            new ArrayList<>(),
            questionRepository.count()
        );
    }

    @Override
    public void createBank(CreateBankRequest request) {
        Bank bank = new Bank();
        bank.setName(request.getName());
        bank.setDesc(request.getDesc());
        bank.setColor(request.getColor());
        bankRepository.save(bank);
    }

    @Override
    public void updateBank(Integer id, UpdateBankRequest request) {
        Bank bank = bankRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("题库不存在"));
        
        if (request.getName() != null) bank.setName(request.getName());
        if (request.getDesc() != null) bank.setDesc(request.getDesc());
        if (request.getColor() != null) bank.setColor(request.getColor());
        
        bankRepository.save(bank);
    }

    @Override
    public void deleteBank(Integer id) {
        Bank bank = bankRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("题库不存在"));
        
        // 检查是否有相关题目
        String categoryId = getCategoryIdByBankName(bank.getName());
        if (categoryId != null) {
            long count = questionRepository.countByCategoryIdContaining(categoryId);
            if (count > 0) {
                throw new IllegalArgumentException("该模块下存在 " + count + " 道题目，请先删除所有题目再删除模块");
            }
        }
        
        bankRepository.delete(bank);
    }
}
