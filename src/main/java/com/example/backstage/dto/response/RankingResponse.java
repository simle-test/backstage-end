package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜响应项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {
    private Integer id;
    private String name;
    private String avatar;
    private String color;
    private String desc;
    private Long solved;
}
