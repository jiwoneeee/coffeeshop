package com.example.coffeeshop.domain.menu.repository;

import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuRepositoryCustom {
    List<MenuDto> findMenus(Category category, String keyword, Pageable pageable);
}
