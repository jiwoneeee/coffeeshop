package com.example.coffeeshop.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em){
        //쿼리를 작성하는 JPAQueryFactory에 EntityManager를 넘겨 사용한다.
        return new JPAQueryFactory(em);
    }
}