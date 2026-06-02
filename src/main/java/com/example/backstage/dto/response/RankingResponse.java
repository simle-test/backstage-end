package com.example.backstage.dto.response;

/**
 * 排行榜响应项
 */
public class RankingResponse {
    private Integer id;
    private String name;
    private String avatar;
    private String color;
    private String desc;
    private Long solved;

    public RankingResponse() {}

    public RankingResponse(Integer id, String name, String avatar, String color, String desc, Long solved) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.color = color;
        this.desc = desc;
        this.solved = solved;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public Long getSolved() { return solved; }
    public void setSolved(Long solved) { this.solved = solved; }
}
