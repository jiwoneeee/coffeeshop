package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.repository.MenuRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    private final MenuRepositoryImpl menuRepository;

    // 메뉴 목록 조회
    public List<MenuDto> getAll(Category category, String keyword, Pageable pageable) {
        return menuRepository.findMenus(category, keyword, pageable);
    }

    // 인기 메뉴 목록 조회

}
