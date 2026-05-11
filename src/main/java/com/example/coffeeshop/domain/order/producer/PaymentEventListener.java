package com.example.coffeeshop.domain.order.producer;

import com.example.coffeeshop.domain.order.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.example.coffeeshop.common.config.kafka.KafkaConfig.TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(PaymentEvent event) {
        log.info("[Producer] 이벤트 발행: type={}, memberId={}", event.type(), event.memberId());
        kafkaTemplate.send(TOPIC, String.valueOf(event.memberId()), event);
    }
}