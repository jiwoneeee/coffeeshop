package com.example.coffeeshop.domain.order.service;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.example.coffeeshop.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {
    private final MenuService menuService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Menu decrease(Long menuId, int quantity) {
        Menu menu = menuService.findById(menuId);
        if (menu.getStatus() != MenuStatus.AVAILABLE) {
            throw new ServiceException(ErrorCode.INVALID_STATUS,
                    "주문할 수 없는 메뉴입니다: " + menu.getName());
        }
        menu.minusStock(quantity);
        return menu;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restore(Long menuId, int quantity) {
        Menu menu = menuService.findById(menuId);
        menu.restoreStock(quantity);
    }
}
