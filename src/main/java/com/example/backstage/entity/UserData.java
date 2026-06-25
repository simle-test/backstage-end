package com.example.backstage.entity;

import jakarta.persistence.*;

/**
 * 用户数据表实体
 */
@Entity
@Table(name = "user_data")
public class UserData {

    @Id
    @Column(name = "data_id")
    private Integer dataId;

    @Column(name = "TodayNumber")
    private Integer todayNumber;

    @Column(name = "TodayCorrect")
    private Integer todayCorrect;

    @Column(name = "TodayCorrectRate")
    private Integer todayCorrectRate;

    @Column(name = "weekData")
    private Integer[] weekData;

    @Column(name = "weekCorrect")
    private Integer[] weekCorrect;

    public Integer getDataId() { return dataId; }
    public void setDataId(Integer dataId) { this.dataId = dataId; }
    public Integer getTodayNumber() { return todayNumber; }
    public void setTodayNumber(Integer todayNumber) { this.todayNumber = todayNumber; }
    public Integer getTodayCorrect() { return todayCorrect; }
    public void setTodayCorrect(Integer todayCorrect) { this.todayCorrect = todayCorrect; }
    public Integer getTodayCorrectRate() { return todayCorrectRate; }
    public void setTodayCorrectRate(Integer todayCorrectRate) { this.todayCorrectRate = todayCorrectRate; }
    public Integer[] getWeekData() { return weekData; }
    public void setWeekData(Integer[] weekData) { this.weekData = weekData; }
    public Integer[] getWeekCorrect() { return weekCorrect; }
    public void setWeekCorrect(Integer[] weekCorrect) { this.weekCorrect = weekCorrect; }
}
