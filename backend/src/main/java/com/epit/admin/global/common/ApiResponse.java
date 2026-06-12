package com.epit.admin.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;
    private LocalDateTime timestamp;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorDetail {
        private String code;
        private String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = true;
        res.data = data;
        res.timestamp = LocalDateTime.now();
        return res;
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = false;
        res.error = new ErrorDetail(code, message);
        res.timestamp = LocalDateTime.now();
        return res;
    }
}
