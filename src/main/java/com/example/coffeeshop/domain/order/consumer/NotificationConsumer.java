package com.example.coffeeshop.domain.order.consumer;

import com.example.coffeeshop.domain.order.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

import static com.example.coffeeshop.common.config.kafka.KafkaTopic.TOPIC;

@Slf4j
@Component
public class NotificationConsumer {

    //consumer별 처리 건수 count
    private final AtomicLong processedCount = new AtomicLong(0);

    @KafkaListener(
            topics = TOPIC,
            groupId = "notification-group"
    )
    public void consume(PaymentEvent event) {
        // 나중에 결제 결과를 알림으로 주는 걸 만들더라도...
        // 지금은 알림 띄우는걸 안 넣었으니까 일단 log 만 찍기
        switch (event.type()) {
            case "PAYMENT_COMPLETED" -> {
                log.info("[notification] 결제 완료 - 주문번호: {}, 회원: {}, 금액: {}원",
                        event.orderId(), event.memberId(), event.totalPrice());
            }
            case "PAYMENT_CANCELLED" -> {
                log.info("[notification] 결제 취소 - 주문번호: {}, 사유: {}",
                        event.orderId(), event.cancelReason());
            }
            default -> log.info("[notification] 내부 오류");
        }

        long count = processedCount.incrementAndGet();
        if (count % 100 == 0) {
            log.info("[notification] {}건 처리 완료", count);
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}