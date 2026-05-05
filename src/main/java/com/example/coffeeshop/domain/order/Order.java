package com.example.coffeeshop.domain.order;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id")
    private Long memberId;

    private Long totalPrice;

    private OrderStatus status;

    private LocalDateTime orderedAt;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private CancelReason cancelReason;

    public Order(Long memberId, Long totalPrice) {
        this.memberId = memberId;
        this.totalPrice = totalPrice;
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
}
