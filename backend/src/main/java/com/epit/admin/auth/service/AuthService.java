package com.epit.admin.auth.service;

import com.epit.admin.auth.dto.LoginRequest;
import com.epit.admin.auth.dto.TokenResponse;
import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.global.security.CustomUserDetails;
import com.epit.admin.global.security.jwt.JwtTokenProvider;
import com.epit.admin.member.entity.Member;
import com.epit.admin.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_PREFIX = "refresh:";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Member member = memberRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            if (!member.isEnabled()) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            }

            member.updateLoginTime();

            String accessToken = jwtTokenProvider.createAccessToken(
                    member.getId(), member.getUsername(), member.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getUsername());

            // Redis에 리프레시 토큰 저장 (14일)
            redisTemplate.opsForValue().set(REFRESH_PREFIX + member.getUsername(),
                    refreshToken, 14, TimeUnit.DAYS);

            return buildTokenResponse(accessToken, refreshToken, member);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public TokenResponse refresh(String refreshToken) {
        String username = jwtTokenProvider.getUsername(refreshToken);

        String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + username);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(
                member.getId(), member.getUsername(), member.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getUsername());

        redisTemplate.opsForValue().set(REFRESH_PREFIX + username, newRefreshToken, 14, TimeUnit.DAYS);

        return buildTokenResponse(newAccessToken, newRefreshToken, member);
    }

    public void logout(String username) {
        redisTemplate.delete(REFRESH_PREFIX + username);
    }

    private TokenResponse buildTokenResponse(String accessToken, String refreshToken, Member member) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(30 * 60L)
                .member(TokenResponse.MemberSummary.builder()
                        .id(member.getId())
                        .username(member.getUsername())
                        .name(member.getName())
                        .role(member.getRole().name())
                        .build())
                .build();
    }
}
