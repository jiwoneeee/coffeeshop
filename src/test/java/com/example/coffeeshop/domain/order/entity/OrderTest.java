package com.example.coffeeshop.domain.order.entity;

import com.example.coffeeshop.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(1L);
    }

    @Nested
    @DisplayName("결제 처리")
    class PaidTest {

        @Test
        @DisplayName("성공 - PENDING 상태에서 결제")
        void paid_success() {
            order.paid();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - PAID 상태에서 재결제 시도")
        void paid_alreadyPaid() {
            order.paid();

            assertThatThrownBy(() -> order.paid())
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - CANCELLED 상태에서 결제 시도")
        void paid_alreadyCancelled() {
            order.cancelled(CancelReason.USER_CANCEL);

            assertThatThrownBy(() -> order.paid())
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("취소 처리")
    class CancelledTest {

        @Test
        @DisplayName("성공 - PENDING 상태에서 취소")
        void cancelled_success() {
            order.cancelled(CancelReason.USER_CANCEL);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelReason()).isEqualTo(CancelReason.USER_CANCEL);
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - PAID 상태에서 취소 시도")
        void cancelled_alreadyPaid() {
            order.paid();

            assertThatThrownBy(() -> order.cancelled(CancelReason.USER_CANCEL))
                    .isInstanceOf(ServiceException.class);
        }
    }
}
