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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 题库服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceImpl implements BankService {

    private final BankRepository bankRepository;
    private final QuestionRepository questionRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<BankResponse> getBankList() {
        List<Bank> banks = bankRepository.findAll();
        List<BankResponse> responses = new ArrayList<>();
        
        for (Bank bank : banks) {
            responses.add(new BankResponse(
                bank.getId(),
                bank.getName(),
                bank.getDesc(),
                bank.getColor(),
                questionRepository.count(),
                bank.getUpdatedAt() != null ? bank.getUpdatedAt().format(FORMATTER) : ""
            ));
        }
        
        return responses;
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
}
