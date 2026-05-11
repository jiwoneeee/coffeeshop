package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.example.coffeeshop.domain.menu.service.MenuService;
import com.example.coffeeshop.domain.order.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private MenuService menuService;

    private Menu menu;

    @BeforeEach
    void setUp() {
        menu = new Menu("아메리카노", Category.COFFEE, 4500L, 10);
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.AVAILABLE);
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseTest {

        @Test
        @DisplayName("성공 - 정상 차감")
        void decrease_success() {
            given(menuService.findById(1L)).willReturn(menu);

            Menu result = stockService.decrease(1L, 3);

            assertThat(result.getStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("성공 - 전량 차감 시 SOLD_OUT")
        void decrease_soldOut() {
            given(menuService.findById(1L)).willReturn(menu);

            Menu result = stockService.decrease(1L, 10);

            assertThat(result.getStock()).isEqualTo(0);
            assertThat(result.getStatus()).isEqualTo(MenuStatus.SOLD_OUT);
        }

        @Test
        @DisplayName("실패 - 재고 부족")
        void decrease_insufficient() {
            given(menuService.findById(1L)).willReturn(menu);

            assertThatThrownBy(() -> stockService.decrease(1L, 11))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - AVAILABLE이 아닌 메뉴")
        void decrease_invalidStatus() {
            ReflectionTestUtils.setField(menu, "status", MenuStatus.DELETED);
            given(menuService.findById(1L)).willReturn(menu);

            assertThatThrownBy(() -> stockService.decrease(1L, 1))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - SOLD_OUT 메뉴")
        void decrease_soldOutMenu() {
            ReflectionTestUtils.setField(menu, "status", MenuStatus.SOLD_OUT);
            given(menuService.findById(1L)).willReturn(menu);

            assertThatThrownBy(() -> stockService.decrease(1L, 1))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("재고 복구")
    class RestoreTest {

        @Test
        @DisplayName("성공 - 정상 복구")
        void restore_success() {
            ReflectionTestUtils.setField(menu, "stock", 0);
            ReflectionTestUtils.setField(menu, "status", MenuStatus.SOLD_OUT);
            given(menuService.findById(1L)).willReturn(menu);

            stockService.restore(1L, 5);

            assertThat(menu.getStock()).isEqualTo(5);
            assertThat(menu.getStatus()).isEqualTo(MenuStatus.AVAILABLE);
        }
    }
}