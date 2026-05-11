package com.example.coffeeshop.domain.member.entity;

import com.example.coffeeshop.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("테스트유저");
        ReflectionTestUtils.setField(member, "point", 10000L);
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePointTest {

        @Test
        @DisplayName("성공 - 정상 충전")
        void chargePoint_success() {
            member.chargePoint(5000L);

            assertThat(member.getPoint()).isEqualTo(15000L);
        }

        @Test
        @DisplayName("실패 - 0 이하 금액 충전")
        void chargePoint_invalidAmount() {
            assertThatThrownBy(() -> member.chargePoint(0L))
                    .isInstanceOf(ServiceException.class);

            assertThatThrownBy(() -> member.chargePoint(-1000L))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UsePointTest {

        @Test
        @DisplayName("성공 - 정상 사용")
        void usePoint_success() {
            member.usePoint(3000L);

            assertThat(member.getPoint()).isEqualTo(7000L);
        }

        @Test
        @DisplayName("성공 - 전액 사용")
        void usePoint_exactBalance() {
            member.usePoint(10000L);

            assertThat(member.getPoint()).isEqualTo(0L);
        }

        @Test
        @DisplayName("실패 - 잔액 부족")
        void usePoint_insufficient() {
            assertThatThrownBy(() -> member.usePoint(20000L))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 0 이하 금액 사용")
        void usePoint_invalidAmount() {
            assertThatThrownBy(() -> member.usePoint(0L))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("포인트 적립")
    class EarnPointTest {

        @Test
        @DisplayName("성공 - 정상 적립")
        void earnPoint_success() {
            member.earnPoint(500L);

            assertThat(member.getPoint()).isEqualTo(10500L);
        }
    }
}