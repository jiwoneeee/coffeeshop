package com.example.coffeeshop.domain.order.dto;

public record OrderItemDto(
        Long menuId,
        Integer quantity
) {
}
