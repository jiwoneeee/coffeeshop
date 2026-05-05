package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.member.service.PointService;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.example.coffeeshop.domain.menu.service.MenuRankingService;
import com.example.coffeeshop.domain.menu.service.MenuService;
import com.example.coffeeshop.domain.order.dto.CreateOrderRequest;
import com.example.coffeeshop.domain.order.dto.CreateOrderResponse;
import com.example.coffeeshop.domain.order.dto.OrderItemDto;
import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.entity.Order;
import com.example.coffeeshop.domain.order.entity.OrderItem;
import com.example.coffeeshop.domain.order.repository.OrderItemRepository;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final PointService pointService;
    private final MenuService menuService;
    private final MenuRankingService menuRankingService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public CreateOrderResponse order(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request.userId()));

        long totalPrice = 0L;

        for (OrderItemDto item : request.items()) {
            Menu menu = menuService.findById(item.menuId());

            // 메뉴 상태 확인
            if (menu.getStatus() != MenuStatus.AVAILABLE) {
                throw new ServiceException(ErrorCode.INVALID_STATUS, "주문할 수 없는 메뉴입니다: " + menu.getName());
            }

            menu.minusStock(item.quantity());

            orderItemRepository.save(
                    new OrderItem(order.getId(), menu.getId(), item.quantity(), menu.getPrice())
            );

            totalPrice += menu.getPrice() * item.quantity();
        }

        order.updateTotalPrice(totalPrice);
        return CreateOrderResponse.from(order);
    }

    @Transactional
    public void pay(Long orderId) {
        Order order = findById(orderId);

        pointService.use(order.getMemberId(), order.getTotalPrice());
        pointService.earn(order.getMemberId(), order.getTotalPrice());

        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        items.forEach(item ->
                menuRankingService.count(item.getMenuId(), LocalDate.now())
        );

        order.paid();
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = findById(orderId);
        restoreByOrderId(orderId);
        order.cancelled(CancelReason.USER_CANCEL);
    }

    public Order findById(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(
                () -> new ServiceException(ErrorCode.ORDER_NOT_FOUND)
        );
    }

    public void restoreByOrderId(Long orderId){
        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);

        items.forEach(item -> {
            Menu menu = menuService.findById(item.getMenuId());
            menu.restoreStock(item.getQuantity());
        });
    }
}
