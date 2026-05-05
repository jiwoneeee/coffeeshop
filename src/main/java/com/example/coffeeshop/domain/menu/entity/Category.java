package com.example.coffeeshop.domain.menu.entity;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;

public enum Category {
    COFFEE, TEA, JUICE, DESSERT;

    public static Category from(String category) {
        if (category == null) {
            return null;
        }
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCode.INVALID_CATEGORY,
                    "유효하지 않은 카테고리입니다: " + category);
        }
    }
}
