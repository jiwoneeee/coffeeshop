package com.example.coffeeshop.domain.menu.service;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.dto.RankingMenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.Menu;
import com.example.coffeeshop.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuRankingService menuRankingService;

    // 메뉴 목록 조회
    public List<MenuDto> getAll(Category category, String keyword, Pageable pageable) {
        return menuRepository.findMenus(category, keyword, pageable);
    }

    // 인기 메뉴 목록 조회
    public List<RankingMenuDto> getTop3() {
        return menuRankingService.findTop3Today().stream()
                .map(ranking -> {
                    Menu menu = menuRepository.findById(Long.parseLong(ranking.getTitle()))
                            .orElseThrow(() -> new ServiceException(ErrorCode.MENU_NOT_FOUND));
                    return RankingMenuDto.of(menu, ranking.getScore());
                })
                .toList();
    }

}
