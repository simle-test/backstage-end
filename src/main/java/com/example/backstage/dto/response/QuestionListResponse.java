package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目列表响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListResponse {
    private List<QuestionListItem> list;
    private Long total;
    private Integer page;
    private Integer size;
}
