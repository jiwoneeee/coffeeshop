package com.example.coffeeshop.domain.menu.dto;

import com.example.coffeeshop.domain.menu.entity.Menu;

public record MenuDto(
        Long id,
        String name,
        String category,
        Long price,
        Integer stock,
        String status
) {

    public static MenuDto from (Menu menu){
        return new MenuDto(
                menu.getId(),
                menu.getName(),
                menu.getCategory().toString(),
                menu.getPrice(),
                menu.getStock(),
                menu.getStatus().toString()
        );
    }
}
