package com.example.coffeeshop.domain.menu.entity;

import com.example.coffeeshop.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class MenuTest {

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu("아메리카노", Category.COFFEE, 4500L, 10);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("재고 차감")
    class MinusStockTest {

        @Test
        @DisplayName("성공 - 정상 차감")
        void minusStock_success() {
            menu.minusStock(3);

            assertThat(menu.getStock()).isEqualTo(7);
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.AVAILABLE);
        }

        @Test
        @DisplayName("성공 - 재고 전부 소진 시 SOLD_OUT 상태로 변경")
        void minusStock_soldOut() {
            menu.minusStock(10);

            assertThat(menu.getStock()).isEqualTo(0);
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("실패 - 재고 부족")
        void minusStock_insufficient() {
            assertThatThrownBy(() -> menu.minusStock(11))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 0 이하 수량 차감")
        void minusStock_invalidAmount() {
            assertThatThrownBy(() -> menu.minusStock(0))
                    .isInstanceOf(ServiceException.class);

            assertThatThrownBy(() -> menu.minusStock(-1))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreStockTest {

        @Test
        @DisplayName("성공 - 정상 복구")
        void restoreStock_success() {
            menu.minusStock(10); // SOLD_OUT 상태
            menu.restoreStock(5);

            assertThat(menu.getStock()).isEqualTo(5);
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.AVAILABLE);
        }
    }
}