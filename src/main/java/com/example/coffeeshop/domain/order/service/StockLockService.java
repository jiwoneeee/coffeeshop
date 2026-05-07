package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.annotation.DistributedLock;
import com.example.coffeeshop.domain.menu.entity.Menu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockLockService {
    private final StockService stockService;

    @DistributedLock(key = "'stock:' + #menuId", waitTime = 10, leaseTime = -1)
    public Menu decrease(Long menuId, int quantity) {
        return stockService.decrease(menuId, quantity);
    }

    @DistributedLock(key = "'stock:' + #menuId", waitTime = 10, leaseTime = -1)
    public void restore(Long menuId, int quantity) {
        stockService.restore(menuId, quantity);
    }
}
