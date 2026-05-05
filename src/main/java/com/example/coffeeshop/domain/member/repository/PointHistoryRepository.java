package com.example.coffeeshop.domain.member.repository;

import com.example.coffeeshop.domain.member.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
