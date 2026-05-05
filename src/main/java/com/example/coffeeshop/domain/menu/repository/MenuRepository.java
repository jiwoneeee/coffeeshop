package com.example.coffeeshop.domain.menu.repository;

import com.example.coffeeshop.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {
}