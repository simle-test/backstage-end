package com.example.backstage.dto.response;

import java.util.List;

/**
 * 用户列表响应
 */
public class UserListResponse {
    private List<UserListItem> list;
    private Long total;
    private Integer page;
    private Integer size;

    public UserListResponse() {}

    public UserListResponse(List<UserListItem> list, Long total, Integer page, Integer size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    // Getters and Setters
    public List<UserListItem> getList() { return list; }
    public void setList(List<UserListItem> list) { this.list = list; }
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
