package com.example.coffeeshop.common.util;

import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.entity.OrderStatus;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.example.coffeeshop.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AutoCancelScheduler {
    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Scheduled(cron = "0 * * * * *")
    public void orderExpire(){
        List<Long> expiredOrderIds =
                orderRepository.findExpiredOrders(
                        LocalDateTime.now().minusMinutes(10)
                );
        log.info("[AutoCancelScheduler] scheduling 시작, 확인 건수: {}", expiredOrderIds.size());

        for (Long orderId : expiredOrderIds) {
            try {
                orderService.cancel(orderId, CancelReason.TIME_OUT);
            } catch (ObjectOptimisticLockingFailureException e){
                log.info("[AutoCancelScheduler] 이미 다른 트랜잭션에서 처리됨 orderId={}", orderId, e);
            } catch (Exception e) {
                log.error("[AutoCancelScheduler] 불특정 오류 발생 orderId = {}", orderId, e);
            }
        }
    }
}
