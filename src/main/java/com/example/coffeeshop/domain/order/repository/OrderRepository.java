package com.example.coffeeshop.domain.order.repository;

import com.example.coffeeshop.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
