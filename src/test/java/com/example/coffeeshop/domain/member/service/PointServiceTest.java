package com.example.coffeeshop.domain.member.service;

import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.member.service.PointService;
import com.example.coffeeshop.domain.member.dto.ChargePointRequest;
import com.example.coffeeshop.domain.member.dto.ChargePointResponse;
import com.example.coffeeshop.domain.member.entity.PointHistory;
import com.example.coffeeshop.domain.member.repository.PointHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member("테스트유저");
        ReflectionTestUtils.setField(member, "id", 1L);
        ReflectionTestUtils.setField(member, "point", 50000L);
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargeTest {

        @Test
        @DisplayName("성공 - 정상 충전")
        void charge_success() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            ChargePointResponse response = pointService.charge(1L, new ChargePointRequest(10000L));

            assertThat(response.memberId()).isEqualTo(1L);
            assertThat(response.chargedAmount()).isEqualTo(10000L);
            assertThat(response.currentBalance()).isEqualTo(60000L);
            verify(pointHistoryRepository).save(any(PointHistory.class));
        }

        @Test
        @DisplayName("실패 - 최소 충전 금액 미만")
        void charge_belowMinimum() {
            assertThatThrownBy(() -> pointService.charge(1L, new ChargePointRequest(500L)))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 1회 최대 충전 금액 초과")
        void charge_exceedMaxPerCharge() {
            assertThatThrownBy(() -> pointService.charge(1L, new ChargePointRequest(2_000_000L)))
                    .isInstanceOf(ServiceException.class);
        }

        @Test
        @DisplayName("실패 - 최대 보유 금액 초과")
        void charge_exceedMaxBalance() {
            ReflectionTestUtils.setField(member, "point", 9_500_000L);
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            assertThatThrownBy(() -> pointService.charge(1L, new ChargePointRequest(600_000L)))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class UsePointTest {

        @Test
        @DisplayName("성공 - 정상 사용")
        void usePoint_success() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            pointService.usePoint(1L, 10000L);

            assertThat(member.getPoint()).isEqualTo(40000L);
            verify(pointHistoryRepository).save(any(PointHistory.class));
        }

        @Test
        @DisplayName("실패 - 잔액 부족")
        void usePoint_insufficient() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            assertThatThrownBy(() -> pointService.usePoint(1L, 100000L))
                    .isInstanceOf(ServiceException.class);
        }
    }

    @Nested
    @DisplayName("포인트 적립")
    class EarnPointTest {

        @Test
        @DisplayName("성공 - 10% 적립")
        void earnPoint_success() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            pointService.earnPoint(1L, 10000L);

            // 10000 / 10 = 1000 적립
            assertThat(member.getPoint()).isEqualTo(51000L);
            verify(pointHistoryRepository).save(any(PointHistory.class));
        }
    }
}