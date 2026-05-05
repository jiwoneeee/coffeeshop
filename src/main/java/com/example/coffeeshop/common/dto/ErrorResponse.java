package com.example.coffeeshop.common.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status; // 404
    private final String error; // NOT_FOUND
    private final String code; // E001
    private final String message; // 존재하지 않는 사용자입니다.
    private final String path; // 요청 api 경로
}