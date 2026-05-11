package com.example.coffeeshop.domain.member.dto;


import org.hibernate.validator.constraints.Range;

public record ChargePointRequest(
        @Range(min = 1_000L, max = 1_000_000L)
        Long point
) {
}
