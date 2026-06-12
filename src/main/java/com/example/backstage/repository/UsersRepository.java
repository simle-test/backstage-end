package com.example.backstage.repository;

import com.example.backstage.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * users表数据访问层
 */
@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Integer> {

    /**
     * 查询所有用户（使用原生SQL）
     */
    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<Object[]> findAllUsersNative();

    /**
     * 统计用户数量（使用原生SQL）
     */
    @Query(value = "SELECT COUNT(*) FROM users", nativeQuery = true)
    long countUsersNative();
}