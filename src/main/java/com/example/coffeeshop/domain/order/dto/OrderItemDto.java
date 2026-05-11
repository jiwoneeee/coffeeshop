package com.example.coffeeshop.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemDto(
        @NotNull
        Long menuId,
        @Min(1)
        Integer quantity
) {
}
