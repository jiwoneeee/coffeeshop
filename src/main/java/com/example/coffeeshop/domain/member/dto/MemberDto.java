package com.example.coffeeshop.domain.member.dto;

import com.example.coffeeshop.domain.member.entity.Member;

public record MemberDto(
        Long id,
        String name,
        Long point) {

    public static MemberDto from(Member member) {
        return new MemberDto(
                member.getId(),
                member.getName(),
                member.getPoint()
        );
    }
}
