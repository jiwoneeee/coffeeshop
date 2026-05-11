package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.domain.menu.dto.RankingDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MenuRankingServiceTest {

    @InjectMocks
    private MenuRankingService menuRankingService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("점수 적산")
    class CountTest {

        @Test
        @DisplayName("성공 - 점수 증가 및 TTL 설정")
        void count_success_withTtl() {
            String key = "menu:ranking:" + LocalDate.now();
            given(stringRedisTemplate.getExpire(key)).willReturn(-1L);

            menuRankingService.count(1L, 3, LocalDate.now());

            verify(zSetOperations).incrementScore(key, "1", 3);
            verify(stringRedisTemplate).expire(eq(key), eq(7L), eq(java.util.concurrent.TimeUnit.DAYS));
        }

        @Test
        @DisplayName("성공 - 이미 TTL이 있으면 재설정하지 않음")
        void count_success_skipTtl() {
            String key = "menu:ranking:" + LocalDate.now();
            given(stringRedisTemplate.getExpire(key)).willReturn(600000L);

            menuRankingService.count(1L, 2, LocalDate.now());

            verify(zSetOperations).incrementScore(key, "1", 2);
            verify(stringRedisTemplate, never()).expire(anyString(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Top3 조회")
    class FindTop3TodayTest {

        @Test
        @DisplayName("성공 - 캐시 히트")
        void findTop3_cacheHit() throws JsonProcessingException {
            String cachedJson = "[{\"title\":\"1\",\"score\":10.0}]";
            List<RankingDto> expected = List.of(new RankingDto("1", 10.0));

            given(valueOperations.get("menu:ranking:cache")).willReturn(cachedJson);
            given(objectMapper.readValue(eq(cachedJson), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                    .willReturn(expected);

            List<RankingDto> result = menuRankingService.findTop3Today();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("1");
            verify(zSetOperations, never()).reverseRangeWithScores(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("성공 - 캐시 미스 → ZSet 조회 후 캐시 저장")
        void findTop3_cacheMiss() throws JsonProcessingException {
            given(valueOperations.get("menu:ranking:cache")).willReturn(null);

            // ZSet 결과 구성
            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(ZSetOperations.TypedTuple.of("1", 150.0));
            tuples.add(ZSetOperations.TypedTuple.of("3", 100.0));
            tuples.add(ZSetOperations.TypedTuple.of("5", 50.0));

            String key = "menu:ranking:" + LocalDate.now();
            given(zSetOperations.reverseRangeWithScores(key, 0, 2)).willReturn(tuples);
            given(objectMapper.writeValueAsString(any())).willReturn("[...]");

            List<RankingDto> result = menuRankingService.findTop3Today();

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getTitle()).isEqualTo("1");
            assertThat(result.get(0).getScore()).isEqualTo(150.0);
            verify(valueOperations).set(eq("menu:ranking:cache"), anyString(), eq(1L), eq(java.util.concurrent.TimeUnit.HOURS));
        }

        @Test
        @DisplayName("성공 - ZSet 결과가 null이면 빈 리스트 반환")
        void findTop3_emptyResult() {
            given(valueOperations.get("menu:ranking:cache")).willReturn(null);

            String key = "menu:ranking:" + LocalDate.now();
            given(zSetOperations.reverseRangeWithScores(key, 0, 2)).willReturn(null);

            List<RankingDto> result = menuRankingService.findTop3Today();

            assertThat(result).isEmpty();
        }
    }
}