package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.member.service.PointLockService;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.order.dto.*;
import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.entity.Order;
import com.example.coffeeshop.domain.order.entity.OrderItem;
import com.example.coffeeshop.domain.order.producer.PaymentEventListener;
import com.example.coffeeshop.domain.order.repository.OrderItemRepository;
import com.example.coffeeshop.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final PointLockService pointService;
    private final StockLockService stockLockService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final PaymentEventListener paymentEventListener;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public CreateOrderResponse order(CreateOrderRequest request) {
        Member member = memberRepository.findById(request.memberId()).orElseThrow(
                ()-> new ServiceException(ErrorCode.MEMBER_NOT_FOUND)
        );
        Order order = orderRepository.save(new Order(request.memberId()));

        long totalPrice = 0L;

        for (OrderItemDto item : request.items()) {
            Menu menu = stockLockService.decrease(item.menuId(), item.quantity());

            orderItemRepository.save(
                    new OrderItem(order.getId(), menu.getId(), item.quantity(), menu.getPrice())
            );

            totalPrice += menu.getPrice() * item.quantity();
//            log.info("[OrderService] totalPrice: {}", totalPrice);
        }

        if (member.getPoint() < totalPrice) {
            throw new ServiceException(ErrorCode.SHORT_POINT, "잔액이 부족합니다. 현재 잔액: "+member.getPoint());
        }

        order.updateTotalPrice(totalPrice);
        return CreateOrderResponse.from(order);
    }

    @Transactional
    public void pay(Long orderId) {
        Order order = findById(orderId);
        Member member = memberRepository.findById(order.getMemberId()).orElseThrow(
                ()-> new ServiceException(ErrorCode.MEMBER_NOT_FOUND)
        );

        if (member.getPoint() < order.getTotalPrice()) {
            throw new ServiceException(ErrorCode.SHORT_POINT,
                    "잔액이 부족합니다. 현재 잔액: " + member.getPoint());
        }

        pointService.useAndEarnPoint(order.getMemberId(), order.getTotalPrice());

        order.paid();

        eventPublisher.publishEvent(
                PaymentEvent.completed(
                        order.getId(),
                        order.getMemberId(),
                        order.getTotalPrice(),
                        order.getOrderedAt()
                )
        );
    }

    @Transactional
    public void cancel(Long orderId, CancelReason reason) {
        Order order = findById(orderId);

        List<OrderItem> items = orderItemRepository.findAllByOrderId(orderId);
        items.forEach(item ->
                stockLockService.restore(item.getMenuId(), item.getQuantity())
        );

        order.cancelled(reason);

        eventPublisher.publishEvent(
                PaymentEvent.cancelled(
                        order.getId(),
                        order.getMemberId(),
                        order.getTotalPrice(),
                        order.getCancelReason().name()
                )
        );
    }

    public Order findById(Long orderId){
        return orderRepository.findById(orderId).orElseThrow(
                () -> new ServiceException(ErrorCode.ORDER_NOT_FOUND)
        );
    }

    public List<Long> getExpiredOrderIds(LocalDateTime expiredBefore){
        return orderRepository.findExpiredOrders(
                expiredBefore
        );
    }
}
