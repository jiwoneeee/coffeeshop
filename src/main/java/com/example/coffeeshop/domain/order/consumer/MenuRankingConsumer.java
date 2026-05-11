package com.example.coffeeshop.domain.order.consumer;

import com.example.coffeeshop.domain.menu.service.MenuRankingService;
import com.example.coffeeshop.domain.order.dto.PaymentEvent;
import com.example.coffeeshop.domain.order.entity.OrderItem;
import com.example.coffeeshop.domain.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.example.coffeeshop.common.config.kafka.KafkaTopic.TOPIC;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuRankingConsumer {
    private final MenuRankingService menuRankingService;
    private final OrderItemRepository orderItemRepository;

    //consumer별 처리 건수 count
    private final AtomicLong processedCount = new AtomicLong(0);

    @KafkaListener(
            topics = TOPIC,
            groupId = "menu-ranking-group"
    )
    public void consume(PaymentEvent event) {

        if (!event.type().equals("PAYMENT_COMPLETED")) return;

        List<OrderItem> itemList = orderItemRepository.findAllByOrderId(event.orderId());
        log.info("[menu-ranking] 결제 성공 - 수량만큼 Count 된 menu 개수: {}", itemList.size());

        for (OrderItem i : itemList){
            menuRankingService.count(i.getMenuId(), i.getQuantity(), LocalDate.from(event.orderedAt()));
        }

        long count = processedCount.incrementAndGet();
        if (count % 100 == 0) {
            log.info("[ranking] {}건 처리 완료", count);
        }
    }

    public long getProcessedCount() {
        return processedCount.get();
    }
}
