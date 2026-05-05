package com.example.coffeeshop.domain.menu.dto;

import com.example.coffeeshop.domain.menu.entity.Menu;

public record RankingMenuDto(
        Long id,
        String name,
        String category,
        Long price,
        Integer stock,
        String status,
        Long score
) {
    public static RankingMenuDto of (Menu menu, Double score){
        return new RankingMenuDto(
                menu.getId(),
                menu.getName(),
                menu.getCategory().toString(),
                menu.getPrice(),
                menu.getStock(),
                menu.getStatus().toString(),
                score.longValue()
        );
    }
}
