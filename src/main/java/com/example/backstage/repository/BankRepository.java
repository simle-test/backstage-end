package com.example.backstage.repository;

import com.example.backstage.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 题库数据访问层
 */
@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {
}
