package com.example.coffeeshop.domain.order.entity;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_status_ordered_at", columnList = "status, ordered_at")
})
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "member_id")
    private Long memberId;

    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderedAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    private CancelReason cancelReason;

    public Order(Long memberId) {
        this.memberId = memberId;
        this.totalPrice = 0L;
        this.status = OrderStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
    }

    public void paid(){
        if (this.status != OrderStatus.PENDING) throw new ServiceException(ErrorCode.INVALID_STATUS, "대기 상태가 아닙니다.");
        this.status = OrderStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void cancelled(CancelReason cancelReason){
        if (this.status != OrderStatus.PENDING) throw new ServiceException(ErrorCode.INVALID_STATUS, "대기 상태가 아닙니다.");
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void updateTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }
}
