package com.epit.admin.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C002", "유효하지 않은 입력값입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A004", "리프레시 토큰을 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다."),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "A006", "비활성화된 계정입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "M002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "M003", "이미 사용 중인 이메일입니다."),

    // Station
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "충전소를 찾을 수 없습니다."),
    DUPLICATE_STATION_CODE(HttpStatus.CONFLICT, "S002", "이미 사용 중인 충전소 코드입니다."),

    // Charger
    CHARGER_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "충전기를 찾을 수 없습니다."),
    DUPLICATE_CHARGER_CODE(HttpStatus.CONFLICT, "CH002", "이미 사용 중인 충전기 코드입니다."),

    // Violation
    VIOLATION_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "위반 기록을 찾을 수 없습니다."),

    // Internal
    INTERNAL_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "I001", "내부 API 토큰이 유효하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
