package com.example.coffeeshop.domain.member.repository;

import com.example.coffeeshop.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
