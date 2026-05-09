package com.example.coffeeshop.domain.member.service;

import com.example.coffeeshop.common.annotation.DistributedLock;
import com.example.coffeeshop.domain.member.dto.ChargePointRequest;
import com.example.coffeeshop.domain.member.dto.ChargePointResponse;
import com.example.coffeeshop.domain.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointLockService {
    private final PointService pointService;

    @DistributedLock(key = "'point:' + #memberId")
    public ChargePointResponse charge(Long memberId, ChargePointRequest request) {
        return pointService.charge(memberId, request);
    }

//    @DistributedLock(key = "'point:' + #memberId")
//    public void usePoint(Long memberId, Long amount) {
//        pointService.usePoint(memberId, amount);
//    }
//
//    @DistributedLock(key = "'point:' + #memberId")
//    public void earnPoint(Long memberId, Long amount) {
//        pointService.earnPoint(memberId, amount);
//    }

    @DistributedLock(key = "'point:' + #memberId")
    public void calAndSavePoint(Long memberId, Long amount) {
        pointService.usePoint(memberId, amount);
        pointService.earnPoint(memberId, amount);
    }

    public MemberDto getMemberDto(Long memberId) {
        return MemberDto.from(pointService.getMember(memberId));
    }

}