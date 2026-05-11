package com.example.coffeeshop.domain.member.dto;


import org.hibernate.validator.constraints.Range;

public record ChargePointRequest(
        @Range(min = 1_000L, max = 1_000_000L, message = "충전 포인트는 최소 1,000, 최대 1,000,000 point 입니다.")
        Long point
) {
}
