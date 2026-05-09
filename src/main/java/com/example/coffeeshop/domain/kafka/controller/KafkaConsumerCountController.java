package com.example.coffeeshop.domain.kafka.controller;

import com.example.coffeeshop.common.dto.ApiResponse;
import com.example.coffeeshop.domain.order.consumer.MenuRankingConsumer;
import com.example.coffeeshop.domain.order.consumer.MetricsConsumer;
import com.example.coffeeshop.domain.order.consumer.NotificationConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
public class KafkaConsumerCountController {

    private final NotificationConsumer notificationConsumer;
    private final MetricsConsumer metricsConsumer;
    private final MenuRankingConsumer menuRankingConsumer;

    @GetMapping("/consumer-counts")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getConsumerCounts() {
        Map<String, Long> counts = Map.of(
                "notification-group", notificationConsumer.getProcessedCount(),
                "metrics-group", metricsConsumer.getProcessedCount(),
                "menu-ranking-group", menuRankingConsumer.getProcessedCount()
        );
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(counts));
    }
}