package com.example.coffeeshop.domain.order.dto;

import com.example.coffeeshop.domain.order.entity.Order;

public record CreateOrderResponse(
        Long orderId,
        Long memberId,
        Long totalPrice

) {
    public static CreateOrderResponse from(Order order){
        return new CreateOrderResponse(
                order.getId(),
                order.getMemberId(),
                order.getTotalPrice()
        );
    }
}
