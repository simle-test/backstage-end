package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题库详情响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailResponse {
    private Integer id;
    private String name;
    private String desc;
    private String color;
    private List<QuestionListItem> questions;
    private Long count;
}
