package com.example.backstage.config;

import com.example.backstage.entity.Bank;
import com.example.backstage.repository.BankRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 数据初始化配置
 */
@Component
public class DataInitializer {

    private final BankRepository bankRepository;

    public DataInitializer(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    @PostConstruct
    public void init() {
        if (bankRepository.count() == 0) {
            List<Bank> banks = Arrays.asList(
                createBank("政治理论", "政治理论题库，包含最新时政热点", "#409EFF"),
                createBank("数量关系", "数量关系题目，包含数学运算和数字推理", "#67C23A"),
                createBank("资料分析", "资料分析题目，包含图表分析和数据计算", "#E6A23C"),
                createBank("常识判断", "常识判断题，涵盖法律、科技、人文等领域", "#F56C6C"),
                createBank("判断推理", "判断推理题目，包含图形推理和逻辑判断", "#909399"),
                createBank("言语理解", "言语理解与表达题目", "#B37FEB")
            );
            bankRepository.saveAll(banks);
        }
    }

    private Bank createBank(String name, String desc, String color) {
        Bank bank = new Bank();
        bank.setName(name);
        bank.setDesc(desc);
        bank.setColor(color);
        return bank;
    }
}
