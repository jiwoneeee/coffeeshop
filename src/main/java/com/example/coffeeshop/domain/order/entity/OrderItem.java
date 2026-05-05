package com.example.coffeeshop.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "menu_id")
    private Long menuId;

    private Integer quantity;

    private Long price; // snapshot

    public OrderItem(Long orderId, Long menuId, Integer quantity, Long price) {
        this.orderId = orderId;
        this.menuId = menuId;
        this.quantity = quantity;
        this.price = price;
    }
}
