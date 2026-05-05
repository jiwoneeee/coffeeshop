package com.example.coffeeshop.domain.menu.dto;

import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;

public record MenuDto(
        Long id,
        String name,
        Category category,
        Long price,
        Integer stock,
        MenuStatus status
) {

    public static MenuDto from (Menu menu){
        return new MenuDto(
                menu.getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getStock(),
                menu.getStatus()
        );
    }
}
