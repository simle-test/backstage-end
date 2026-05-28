package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListItem {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String roleText;
    private Long practiceCount;
    private Double passRate;
    private String status;
    private String statusText;
    private String joinDate;
}
