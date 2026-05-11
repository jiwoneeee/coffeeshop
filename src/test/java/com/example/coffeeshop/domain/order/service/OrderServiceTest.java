package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.order.dto.CreateOrderRequest;
import com.example.coffeeshop.domain.order.dto.CreateOrderResponse;
import com.example.coffeeshop.domain.order.dto.OrderItemDto;
import com.example.coffeeshop.domain.order.dto.PaymentEvent;
import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.entity.Order;
import com.example.coffeeshop.domain.order.entity.OrderItem;
import com.example.coffeeshop.domain.order.entity.OrderStatus;
import com.example.coffeeshop.domain.order.repository.OrderItemRepository;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import com.example.coffeeshop.domain.member.service.PointLockService;
import com.example.coffeeshop.domain.order.service.StockLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StockLockService stockLockService;
    @Mock
    private PointLockService pointService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private Member member;
    private Menu menu;
    private Order order;

    @BeforeEach
    void setUp() {
        member = new Member("테스트유저");
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "point", 50000L);

        menu = new Menu("아메리카노", Category.COFFEE, 4500L, 100);
        ReflectionTestUtils.setField(menu, "id", 1L);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.AVAILABLE);

        order = new Order(1L);
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "totalPrice", 9000L);
    }

    @Nested
    @DisplayName("주문 생성")
    class OrderTest {

        @Test
        @DisplayName("성공 - 정상 주문 생성")
        void order_success() {
            // given
            List<OrderItemDto> items = List.of(new OrderItemDto(1L, 2));
            CreateOrderRequest request = new CreateOrderRequest(1L, items);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(stockLockService.decrease(1L, 2)).willReturn(menu);

            // when
            CreateOrderResponse response = orderService.order(request);

            // then
            assertThat(response.orderId()).isEqualTo(1L);
            assertThat(response.memberId()).isEqualTo(1L);
            verify(stockLockService).decrease(1L, 2);
            verify(orderItemRepository).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원")
        void order_memberNotFound() {
            // given
            List<OrderItemDto> items = List.of(new OrderItemDto(1L, 1));
            CreateOrderRequest request = new CreateOrderRequest(999L, items);

            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.order(request))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 잔액 부족")
        void order_shortPoint() {
            // given
            ReflectionTestUtils.setField(member, "point", 1000L);

            List<OrderItemDto> items = List.of(new OrderItemDto(1L, 2));
            CreateOrderRequest request = new CreateOrderRequest(1L, items);

            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(orderRepository.save(any(Order.class))).willReturn(order);
            given(stockLockService.decrease(1L, 2)).willReturn(menu);

            // when & then
            assertThatThrownBy(() -> orderService.order(request))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("결제")
    class PayTest {

        @Test
        @DisplayName("성공 - 정상 결제")
        void pay_success() {
            // given
            Order payOrder = new Order(1L);
            ReflectionTestUtils.setField(payOrder, "id", 1L);
            ReflectionTestUtils.setField(payOrder, "totalPrice", 9000L);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            orderService.pay(1L);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            verify(pointService).useAndEarnPoint(1L, 9000L);
            verify(eventPublisher).publishEvent(any(PaymentEvent.class));
        }

        @Test
        @DisplayName("실패 - 잔액 부족")
        void pay_shortPoint() {
            // given
            ReflectionTestUtils.setField(member, "point", 1000L);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> orderService.pay(1L))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 이미 결제된 주문")
        void pay_alreadyPaid() {
            // given
            order.paid(); // PENDING → PAID
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(() -> orderService.pay(1L))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("취소")
    class CancelTest {

        @Test
        @DisplayName("성공 - 정상 취소")
        void cancel_success() {
            // given
            Order payOrder = new Order(1L);
            ReflectionTestUtils.setField(payOrder, "id", 1L);
            ReflectionTestUtils.setField(payOrder, "totalPrice", 9000L);

            OrderItem item = new OrderItem(1L, 1L, 2, 4500L);
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findAllByOrderId(1L)).willReturn(List.of(item));

            // when
            orderService.cancel(1L, CancelReason.USER_CANCEL);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelReason()).isEqualTo(CancelReason.USER_CANCEL);
            verify(stockLockService).restore(1L, 2);
            verify(eventPublisher).publishEvent(any(PaymentEvent.class));
        }

        @Test
        @DisplayName("실패 - 이미 취소된 주문")
        void cancel_alreadyCancelled() {
            // given
            order.cancelled(CancelReason.USER_CANCEL); // PENDING → CANCELLED
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));
            given(orderItemRepository.findAllByOrderId(1L)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> orderService.cancel(1L, CancelReason.TIME_OUT))
                    .isInstanceOf(ServiceException.class);
        }
    }
}