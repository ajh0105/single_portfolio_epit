package com.epit.admin.member.dto;

import com.epit.admin.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberCreateRequest {

    @NotBlank @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotNull
    private Member.Role role;

    @NotBlank @Size(min = 8)
    private String password;
}
