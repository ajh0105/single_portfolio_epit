package com.epit.admin.member.dto;

import com.epit.admin.member.entity.Member;
import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String username,
        String name,
        String email,
        String role,
        boolean enabled,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    public static MemberResponse from(Member m) {
        return new MemberResponse(
                m.getId(), m.getUsername(), m.getName(), m.getEmail(),
                m.getRole().name(), m.isEnabled(), m.getLastLoginAt(),
                m.getCreatedAt()
        );
    }
}
