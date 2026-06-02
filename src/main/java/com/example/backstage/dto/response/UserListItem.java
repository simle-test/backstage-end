package com.example.backstage.dto.response;

/**
 * 用户列表项
 */
public class UserListItem {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String roleText;
    private Long practiceCount;
    private Double passRate;
    private String status;
    private String statusText;
    private String joinDate;
    private String avatar;
    private String color;

    public UserListItem() {}

    public UserListItem(Long id, String username, String email, String role, String roleText,
                       Long practiceCount, Double passRate, String status, String statusText, String joinDate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.roleText = roleText;
        this.practiceCount = practiceCount;
        this.passRate = passRate;
        this.status = status;
        this.statusText = statusText;
        this.joinDate = joinDate;
    }

    public UserListItem(Long id, String username, String email, String role, String roleText,
                       Long practiceCount, Double passRate, String joinDate, String status, 
                       String statusText, String avatar, String color) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.roleText = roleText;
        this.practiceCount = practiceCount;
        this.passRate = passRate;
        this.joinDate = joinDate;
        this.status = status;
        this.statusText = statusText;
        this.avatar = avatar;
        this.color = color;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getRoleText() { return roleText; }
    public void setRoleText(String roleText) { this.roleText = roleText; }
    public Long getPracticeCount() { return practiceCount; }
    public void setPracticeCount(Long practiceCount) { this.practiceCount = practiceCount; }
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
