package com.example.coffeeshop.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    private Long amount;

    private Long afterPoint;

    private PointType type;

    private LocalDateTime createdAt;

    public PointHistory(Long memberId, Long amount, Long afterPoint, PointType type) {
        this.memberId = memberId;
        this.amount = amount;
        this.afterPoint = afterPoint;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}
