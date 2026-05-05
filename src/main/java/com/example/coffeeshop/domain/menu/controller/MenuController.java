package com.example.coffeeshop.domain.menu.controller;

import com.example.coffeeshop.common.dto.ApiResponse;
import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.dto.RankingMenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
@Slf4j
public class MenuController {
    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuDto>>> getAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 5) Pageable pageable){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(menuService.getAll(Category.from(category), keyword, pageable)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<RankingMenuDto>>> getTop3() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(menuService.getTop3()));
    }
}
