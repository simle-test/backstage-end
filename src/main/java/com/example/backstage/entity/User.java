
package com.example.backstage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * 用户实体类
 */
@Entity
@Table(name = "end_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 20)
    private String status = "active";

    @Column(length = 255)
    private String avatar;

    @Column(name = "avatar_color", length = 50)
    private String avatarColor;

    @Column(name = "practice_count", nullable = false)
    private Integer practiceCount = 0;

    @Column(name = "pass_rate", nullable = false)
    private Double passRate = 0.0;

    @Column(name = "join_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date joinDate;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        joinDate = new Date();
        updatedAt = new Date();
        if (role == null) {
            role = "user";
        }
        if (status == null) {
            status = "active";
        }
        if (practiceCount == null) {
            practiceCount = 0;
        }
        if (passRate == null) {
            passRate = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
