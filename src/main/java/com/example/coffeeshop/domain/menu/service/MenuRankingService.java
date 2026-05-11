package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.domain.menu.dto.RankingDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class MenuRankingService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public static final String MENU_RANKING_DAILY_KEY = "menu:ranking:";
    public static final String MENU_RANKING_CACHE_KEY = "menu:ranking:cache";
    private static final long CACHE_TTL_HOURS = 1;
    private static final long CACHE_TTL_DAYS = 7;

    public void count(Long menuId, Integer quantity, LocalDate today) {
        String key = MENU_RANKING_DAILY_KEY + today.toString();

        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(menuId), quantity);

        // 키가 새로 생성된 경우에만 TTL 설정 (이미 TTL이 있으면 덮어쓰지 않음)
        if (stringRedisTemplate.getExpire(key) == -1) {
            stringRedisTemplate.expire(key, CACHE_TTL_DAYS, TimeUnit.DAYS);
        }
    }

    public List<RankingDto> findTop3Today(){
        String cached = stringRedisTemplate.opsForValue().get(MENU_RANKING_CACHE_KEY);
        if (cached != null) {
            return deserialize(cached);
        }

        // 캐시 미스 : Z Set 에서 조회
        LocalDate today = LocalDate.now();

        String key = MENU_RANKING_DAILY_KEY + today;

        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(key, 0 ,2 );

        if (result == null ) return Collections.emptyList();

        List<RankingDto> rankings = result.stream()
                .map(tuple -> new RankingDto(tuple.getValue(), tuple.getScore()))
                .toList();

        stringRedisTemplate.opsForValue().set(
                MENU_RANKING_CACHE_KEY,
                serialize(rankings),
                CACHE_TTL_HOURS, TimeUnit.HOURS
        );

        return rankings;
    }

    private String serialize(List<RankingDto> rankings) {
        try {
            return objectMapper.writeValueAsString(rankings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("랭킹 직렬화 실패", e);
        }
    }

    private List<RankingDto> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<RankingDto>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("랭킹 역직렬화 실패", e);
        }
    }
}
