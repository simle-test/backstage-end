package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private List<UserListItem> list;
    private Long total;
    private Integer page;
    private Integer size;
}

/**
 * 用户列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class UserListItem {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String roleText;
    private Long solved;
    private Double passRate;
    private String joinDate;
    private String status;
    private String statusText;
    private String avatar;
    private String color;
}
