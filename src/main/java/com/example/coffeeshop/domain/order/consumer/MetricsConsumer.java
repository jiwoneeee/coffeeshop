package com.example.coffeeshop.domain.order.consumer;

import com.example.coffeeshop.domain.order.dto.PaymentEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static com.example.coffeeshop.common.config.kafka.KafkaConfig.TOPIC;

@Slf4j
@Component
public class MetricsConsumer {

    private final Timer paymentDurationTimer;
    private final Counter paymentSuccessCounter;
    private final Counter paymentCancelCounter;

    //consumer별 처리 건수 count
    private final AtomicLong processedCount = new AtomicLong(0);

    // MeterRegistry는 spring-boot-starter-actuator에 포함되어 있음
    public MetricsConsumer(MeterRegistry meterRegistry) {
        this.paymentDurationTimer = Timer.builder("payment.duration")
                .description("주문 생성 → 결제 완료 소요시간")
                .register(meterRegistry);

        this.paymentSuccessCounter = Counter.builder("payment.success.count")
                .description("결제 성공 횟수")
                .register(meterRegistry);

        this.paymentCancelCounter = Counter.builder("payment.cancel.count")
                .description("결제 취소 횟수")
                .register(meterRegistry);
    }

    @KafkaListener(topics = TOPIC,  groupId = "metrics-group")
    public void consume(PaymentEvent event) {
        switch (event.type()) {
            case "PAYMENT_COMPLETED" -> {
                paymentSuccessCounter.increment();

                // 주문→결제 소요시간 측정
                if (event.orderedAt() != null) {
                    Duration duration = Duration.between(event.orderedAt(), event.occurredAt());
                    paymentDurationTimer.record(duration);

                    log.info("[metrics] 결제 성공 - orderId: {}, 소요시간: {}ms",
                            event.orderId(), duration.toMillis());
                }
            }
            case "PAYMENT_CANCELLED" -> {
                paymentCancelCounter.increment();

                log.info("[metrics] 결제 취소 - orderId: {}, 사유: {}",
                        event.orderId(), event.cancelReason());
            }
        }

        long count = processedCount.incrementAndGet();
        if (count % 100 == 0) {
            log.info("[metrics] {}건 처리 완료", count);
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}