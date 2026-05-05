package com.example.coffeeshop.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SHORT_POINT(HttpStatus.BAD_REQUEST, "P001", "포인트 부족 오류"),
    INVALID_POINT(HttpStatus.BAD_REQUEST, "P002", "포인트 입력값 오류"),

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "M001", "재고 부족 오류"),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "M002", "재고 입력값 오류"),

    VALID_ERROR(HttpStatus.BAD_REQUEST, "V001", "올바르지 않은 입력값"),

    INVALID_STATUS(HttpStatus.BAD_REQUEST, "S001", "유효하지 않은 상태"),

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 멤버"),

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 카테고리");


    private final HttpStatus status;
    private final String code;
    private final String message;

}
