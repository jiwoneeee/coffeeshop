package com.example.coffeeshop.domain.menu.repository;

import com.example.coffeeshop.domain.menu.dto.MenuDto;
import com.example.coffeeshop.domain.menu.entity.Category;
import com.example.coffeeshop.domain.menu.entity.MenuStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.coffeeshop.domain.menu.entity.QMenu.menu;

@RequiredArgsConstructor
public class MenuRepositoryImpl implements MenuRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MenuDto> findMenus(Category category, String keyword, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(MenuDto.class,
                        menu.id,
                        menu.name,
                        menu.category,
                        menu.price
                ))
                .from(menu)
                .where(
                        categoryEq(category),
                        keywordContains(keyword),
                        menu.status.eq(MenuStatus.AVAILABLE)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(menu.price.asc())
                .fetch();

    }

    private BooleanExpression categoryEq(Category category) {
        return category != null ? menu.category.eq(category) : null;
    }

    // 데이터가 적어서 그냥 fullscan 해도 될 것 같아서 contains 씀
    private BooleanExpression keywordContains(String keyword) {
        return keyword != null ? menu.name.contains(keyword) : null;
    }
}
