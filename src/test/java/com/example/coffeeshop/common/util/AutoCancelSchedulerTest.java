package com.example.coffeeshop.common.util;

import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AutoCancelSchedulerTest {

    @InjectMocks
    private AutoCancelScheduler autoCancelScheduler;

    @Mock
    private OrderService orderService;

    @Test
    @DisplayName("성공 - 만료된 주문 취소")
    void orderExpire_success() {
        given(orderService.getExpiredOrderIds(any(LocalDateTime.class)))
                .willReturn(List.of(1L, 2L, 3L));

        autoCancelScheduler.orderExpire();

        verify(orderService).cancel(1L, CancelReason.TIME_OUT);
        verify(orderService).cancel(2L, CancelReason.TIME_OUT);
        verify(orderService).cancel(3L, CancelReason.TIME_OUT);
    }

    @Test
    @DisplayName("성공 - 만료된 주문 없음")
    void orderExpire_noExpired() {
        given(orderService.getExpiredOrderIds(any(LocalDateTime.class)))
                .willReturn(List.of());

        autoCancelScheduler.orderExpire();

        verify(orderService, never()).cancel(anyLong(), any());
    }

    @Test
    @DisplayName("성공 - 낙관적 락 충돌 시 skip하고 나머지 계속 처리")
    void orderExpire_optimisticLockSkip() {
        given(orderService.getExpiredOrderIds(any(LocalDateTime.class)))
                .willReturn(List.of(1L, 2L, 3L));

        // 2번 주문은 이미 다른 트랜잭션에서 처리됨
        willThrow(new ObjectOptimisticLockingFailureException("Order", 2L))
                .given(orderService).cancel(2L, CancelReason.TIME_OUT);

        autoCancelScheduler.orderExpire();

        verify(orderService).cancel(1L, CancelReason.TIME_OUT);
        verify(orderService).cancel(2L, CancelReason.TIME_OUT);
        verify(orderService).cancel(3L, CancelReason.TIME_OUT);
    }

    @Test
    @DisplayName("성공 - 예외 발생해도 나머지 계속 처리")
    void orderExpire_exceptionContinues() {
        given(orderService.getExpiredOrderIds(any(LocalDateTime.class)))
                .willReturn(List.of(1L, 2L, 3L));

        // 1번 주문에서 예외 발생
        willThrow(new RuntimeException("DB 오류"))
                .given(orderService).cancel(1L, CancelReason.TIME_OUT);

        autoCancelScheduler.orderExpire();

        // 1번 실패해도 2, 3번은 처리
        verify(orderService).cancel(1L, CancelReason.TIME_OUT);
        verify(orderService).cancel(2L, CancelReason.TIME_OUT);
        verify(orderService).cancel(3L, CancelReason.TIME_OUT);
    }
}