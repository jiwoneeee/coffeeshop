package com.example.coffeeshop.domain.order.dto;

import java.util.List;

public record CreateOrderRequest(
        Long memberId,
        List<OrderItemDto> items
) {
}
