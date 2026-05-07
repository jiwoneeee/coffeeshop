package com.example.coffeeshop.domain.member.service;

import com.example.coffeeshop.common.exception.ErrorCode;
import com.example.coffeeshop.common.exception.ServiceException;
import com.example.coffeeshop.domain.member.dto.ChargePointRequest;
import com.example.coffeeshop.domain.member.dto.ChargePointResponse;
import com.example.coffeeshop.domain.member.dto.MemberDto;
import com.example.coffeeshop.domain.member.entity.Member;
import com.example.coffeeshop.domain.member.entity.PointHistory;
import com.example.coffeeshop.domain.member.entity.PointType;
import com.example.coffeeshop.domain.member.repository.MemberRepository;
import com.example.coffeeshop.domain.member.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// transaction 용
@Service
@RequiredArgsConstructor
public class PointService {
    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;


    private static final long EARN_RATE_PERCENT = 10;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChargePointResponse charge(Long memberId, ChargePointRequest request){

        if (request.point() < 1_000) {
            throw new ServiceException(ErrorCode.INVALID_POINT, "최소 충전 금액은 1,000입니다.");
        }

        // 1회 최대 충전 금액이 넘는지 확인
        if (request.point()>1_000_000){
            String message = "최대 충전 금액은 1_000_000 을 넘을 수 없습니다. 현재 입력값: "+request.point();
            throw new ServiceException(ErrorCode.INVALID_POINT, message);
        }

        Member member = getMember(memberId);

        // 총 보유 최대 금액이 넘는지 확인
        if (request.point() + member.getPoint() > 10_000_000){
            String message = "최대 보유 금액은 10_000_000 입니다. 보유 포인트: " +member.getPoint();
            throw new ServiceException(ErrorCode.INVALID_POINT, message);
        }

        member.chargePoint(request.point());

        pointHistoryRepository.save(
                new PointHistory(memberId, request.point(), member.getPoint(), PointType.CHARGE));

        return ChargePointResponse.of(member, request.point());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void usePoint(Long memberId, Long amount) {
        Member member = getMember(memberId);
        member.usePoint(amount);
        pointHistoryRepository.save(
                new PointHistory(memberId, amount, member.getPoint(), PointType.USE));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void earnPoint(Long memberId, Long amount) {
        Member member = getMember(memberId);
        member.earnPoint(amount/EARN_RATE_PERCENT);
        pointHistoryRepository.save(
                new PointHistory(memberId, amount, member.getPoint(), PointType.EARN));
    }


    public Member getMember(Long memberId){
        return memberRepository.findById(memberId).orElseThrow(
                ()-> new ServiceException(ErrorCode.MEMBER_NOT_FOUND)
        );
    }
}
