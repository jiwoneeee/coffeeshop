package com.example.coffeeshop.domain.order.dto;

import java.time.LocalDateTime;

public record PaymentEvent(
        String type,              // PAYMENT_COMPLETED, PAYMENT_CANCELLED
        Long orderId,
        Long memberId,
        Long totalPrice,
        LocalDateTime orderedAt,  // 주문 생성 시각 (소요시간 측정용)
        LocalDateTime occurredAt, // 이벤트 발생 시각
        String cancelReason       // 취소 시에만 사용
) {

    public static PaymentEvent completed(Long orderId, Long memberId,
                                         Long totalPrice, LocalDateTime orderedAt) {
        return new PaymentEvent(
                "PAYMENT_COMPLETED", orderId, memberId,
                totalPrice, orderedAt, LocalDateTime.now(), null
        );
    }

    public static PaymentEvent cancelled(Long orderId, Long memberId,
                                         Long totalPrice, String cancelReason) {
        return new PaymentEvent(
                "PAYMENT_CANCELLED", orderId, memberId,
                totalPrice, null, LocalDateTime.now(), cancelReason
        );
    }
}