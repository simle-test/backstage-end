package com.example.backstage.repository;

import com.example.backstage.entity.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 终端用户数据访问层接口
 */
@Repository
public interface EndUserRepository extends JpaRepository<EndUser, Integer> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<EndUser> findByUsername(String username);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
}
