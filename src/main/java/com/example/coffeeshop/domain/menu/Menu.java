package com.example.coffeeshop.domain.menu;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Category category;

    private Long price;

    private Integer stock;

    private MenuStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Menu(String name, Category category, Long price, Integer stock) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = MenuStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void fillStock(Integer amount){
        if (amount <= 0) throw new ServiceException(ErrorCode.OUT_OF_STOCK, "충전 재고의 입력값은 0 보다 커야 합니다.");
        this.stock += amount;
    }

    public void minusStock(Integer amount){
        if (amount <= 0) throw new ServiceException(ErrorCode.OUT_OF_STOCK, "차감 재고의 입력값은 0 보다 커야 합니다.");
        if ((this.stock - amount) < 0) throw new ServiceException(ErrorCode.OUT_OF_STOCK, "재고가 부족합니다.");
        this.stock -= amount;
    }
}
