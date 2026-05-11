package com.example.coffeeshop.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemDto(
        @NotNull(message = "menuId 는 필수 입력값입니다.")
        Long menuId,
        @Min(value = 1, message = "선택한 menu를 최소 1개 주문해야 합니다.")
        Integer quantity
) {
}
