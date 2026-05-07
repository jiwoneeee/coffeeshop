package com.example.coffeeshop.domain.member.controller;

import com.example.coffeeshop.common.dto.ApiResponse;
import com.example.coffeeshop.domain.member.dto.MemberDto;
import com.example.coffeeshop.domain.member.dto.ChargePointRequest;
import com.example.coffeeshop.domain.member.dto.ChargePointResponse;
import com.example.coffeeshop.domain.member.service.PointLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/points")
public class PointController {

    private final PointLockService pointService;

    // 포인트 충전
    @PostMapping("/charge/{memberId}")
    public ResponseEntity<ApiResponse<ChargePointResponse>> charge(
            @PathVariable Long memberId,
            @RequestBody ChargePointRequest request){
        return ResponseEntity.
                status(HttpStatus.OK).
                body(ApiResponse.success(pointService.charge(memberId, request)));
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberDto>> getMemberDto(
            @PathVariable Long memberId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointService.getMemberDto(memberId)));
    }

}
