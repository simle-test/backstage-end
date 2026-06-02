package com.example.backstage.entity;

import jakarta.persistence.*;

/**
 * 用户数据表实体
 */
@Entity
@Table(name = "user_data")
public class UserData {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "finish")
    private Integer finish;

    @Column(name = "score")
    private Integer score;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "total_num")
    private Integer totalNum;

    @Column(name = "today_num")
    private Integer todayNum;

    @Column(name = "today_time")
    private Integer todayTime;

    @Column(name = "max_combo")
    private Integer maxCombo;

    @Column(name = "current_combo")
    private Integer currentCombo;

    @Column(name = "practice_days")
    private Integer practiceDays;

    @Column(name = "continuous_days")
    private Integer continuousDays;

    @Column(name = "accuracy")
    private Double accuracy;

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getFinish() { return finish; }
    public void setFinish(Integer finish) { this.finish = finish; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Integer getTotalTime() { return totalTime; }
    public void setTotalTime(Integer totalTime) { this.totalTime = totalTime; }
    public Integer getTotalNum() { return totalNum; }
    public void setTotalNum(Integer totalNum) { this.totalNum = totalNum; }
    public Integer getTodayNum() { return todayNum; }
    public void setTodayNum(Integer todayNum) { this.todayNum = todayNum; }
    public Integer getTodayTime() { return todayTime; }
    public void setTodayTime(Integer todayTime) { this.todayTime = todayTime; }
    public Integer getMaxCombo() { return maxCombo; }
    public void setMaxCombo(Integer maxCombo) { this.maxCombo = maxCombo; }
    public Integer getCurrentCombo() { return currentCombo; }
    public void setCurrentCombo(Integer currentCombo) { this.currentCombo = currentCombo; }
    public Integer getPracticeDays() { return practiceDays; }
    public void setPracticeDays(Integer practiceDays) { this.practiceDays = practiceDays; }
    public Integer getContinuousDays() { return continuousDays; }
    public void setContinuousDays(Integer continuousDays) { this.continuousDays = continuousDays; }
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
}
