package com.example.coffeeshop.domain.member.entity;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long point;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Member(String name) {
        this.name = name;
        this.point = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void chargePoint(long amount) {
        if (amount <= 0) throw new ServiceException(ErrorCode.INVALID_POINT, "충전 금액은 0보다 커야 합니다.");
        this.point += amount;
    }

    public void usePoint(long amount) {
        if (amount <= 0) throw new ServiceException(ErrorCode.INVALID_POINT, "사용 금액은 0보다 커야 합니다.");
        if (this.point < amount) throw new ServiceException(ErrorCode.INVALID_POINT, "포인트가 부족합니다.");
        this.point -= amount;
    }

    public void earnPoint(long amount) {
        this.point += amount;
    }
}
