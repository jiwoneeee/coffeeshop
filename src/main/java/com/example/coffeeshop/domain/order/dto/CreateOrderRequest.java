package com.example.coffeeshop.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull
        Long memberId,
        @NotEmpty
        @Valid
        List<OrderItemDto> items
) {
}