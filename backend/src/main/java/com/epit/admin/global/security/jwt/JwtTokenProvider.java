package com.epit.admin.global.security.jwt;

import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_UID = "uid";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";

    private final JwtProperties jwtProperties;
    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(
                        jwtProperties.getSecret().getBytes()));
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long memberId, String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claims(Map.of(CLAIM_UID, memberId, CLAIM_ROLE, role, CLAIM_TYPE, TOKEN_TYPE_ACCESS))
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getAccessExpMin() * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claims(Map.of(CLAIM_TYPE, TOKEN_TYPE_REFRESH))
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getRefreshExpDays() * 24 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public Claims validateAndParseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String getUsername(String token) {
        return validateAndParseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return validateAndParseClaims(token).get(CLAIM_ROLE, String.class);
    }

    public boolean isAccessToken(String token) {
        return TOKEN_TYPE_ACCESS.equals(validateAndParseClaims(token).get(CLAIM_TYPE, String.class));
    }
}
