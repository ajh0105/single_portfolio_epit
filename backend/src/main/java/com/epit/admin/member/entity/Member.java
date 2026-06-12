package com.epit.admin.member.entity;

import com.epit.admin.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime lastLoginAt;

    public void updateLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void update(String name, String email, Role role, boolean enabled) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
    }

    public enum Role {
        ADMIN, OPERATOR, VIEWER
    }
}
