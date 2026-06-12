package com.example.backstage.dto.response;

/**
 * 题库响应
 */
public class BankResponse {
    private Integer id;
    private String name;
    private String desc;
    private String color;
    private Long count;
    private String updateTime;

    public BankResponse() {}

    public BankResponse(Integer id, String name, String desc, String color, Long count, String updateTime) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.color = color;
        this.count = count;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}