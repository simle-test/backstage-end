package com.example.backstage.dto.response;

import java.util.List;

/**
 * 题目列表响应
 */
public class QuestionListResponse {
    private List<QuestionListItem> list;
    private Long total;
    private Integer page;
    private Integer size;

    public QuestionListResponse() {}

    public QuestionListResponse(List<QuestionListItem> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<QuestionListItem> getList() { return list; }
    public void setList(List<QuestionListItem> list) { this.list = list; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
