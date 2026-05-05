package com.example.coffeeshop.domain.member.dto;

import com.example.coffeeshop.domain.member.entity.Member;

public record ChargePointResponse(
        Long memberId,
        Long chargedAmount,
        Long currentBalance) {

    public static ChargePointResponse of(Member member, Long chargedAmount) {
        return new ChargePointResponse(
                member.getId(),
                chargedAmount,
                member.getPoint()
        );
    }
}