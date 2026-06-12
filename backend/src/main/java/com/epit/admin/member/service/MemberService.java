package com.epit.admin.member.service;

import com.epit.admin.global.exception.BusinessException;
import com.epit.admin.global.exception.ErrorCode;
import com.epit.admin.member.dto.MemberCreateRequest;
import com.epit.admin.member.dto.MemberResponse;
import com.epit.admin.member.entity.Member;
import com.epit.admin.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<MemberResponse> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable).map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse create(MemberCreateRequest req) {
        if (memberRepository.existsByUsername(req.getUsername()))
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        if (memberRepository.existsByEmail(req.getEmail()))
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        Member member = Member.builder()
                .username(req.getUsername())
                .name(req.getName())
                .email(req.getEmail())
                .role(req.getRole())
                .password(passwordEncoder.encode(req.getPassword()))
                .enabled(true)
                .build();

        return MemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // admin 계정 삭제 방지
        if ("admin".equals(member.getUsername()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        memberRepository.delete(member);
    }
}
