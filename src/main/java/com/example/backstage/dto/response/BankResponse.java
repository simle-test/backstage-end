package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题库响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {
    private Integer id;
    private String name;
    private String desc;
    private String color;
    private Long count;
    private String updateTime;
}
