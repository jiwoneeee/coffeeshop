package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.domain.menu.dto.RankingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class MenuRankingService {
    private final StringRedisTemplate stringRedisTemplate;

    public static final String MENU_RANKING_DAILY_KEY = "menu:ranking:";

    public void count(Long menuId, Integer quantity, LocalDate today) {
        String key = MENU_RANKING_DAILY_KEY + today.toString();

        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(menuId), quantity);
    }

    public List<RankingDto> findTop3Today(){
        LocalDate today = LocalDate.now();

        String key = MENU_RANKING_DAILY_KEY + today;

        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0 ,2 );

        if (result == null ) return Collections.emptyList();

        return result.stream()
                .map(tuple -> new RankingDto(tuple.getValue(), tuple.getScore()))
                .toList();
    }
}
