package com.epit.admin.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private MemberSummary member;

    @Getter
    @Builder
    public static class MemberSummary {
        private Long id;
        private String username;
        private String name;
        private String role;
    }
}
