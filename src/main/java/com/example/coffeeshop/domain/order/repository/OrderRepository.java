package com.example.coffeeshop.domain.order.repository;

import com.example.coffeeshop.domain.order.entity.Order;
import com.example.coffeeshop.domain.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT o.id
        FROM Order o
        WHERE o.status = 'PENDING'
        AND o.orderedAt <= :time
""")
    List<Long> findExpiredOrders(LocalDateTime time);
}
