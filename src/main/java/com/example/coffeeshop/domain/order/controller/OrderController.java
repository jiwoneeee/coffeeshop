package com.example.coffeeshop.domain.order.controller;

import com.example.coffeeshop.common.dto.ApiResponse;
import com.example.coffeeshop.domain.order.dto.CreateOrderRequest;
import com.example.coffeeshop.domain.order.dto.CreateOrderResponse;
import com.example.coffeeshop.domain.order.entity.CancelReason;
import com.example.coffeeshop.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> order(@Valid @RequestBody CreateOrderRequest request){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.order(request)));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<String>> pay(@PathVariable Long orderId){
        orderService.pay(orderId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("결제 성공"));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancel(@PathVariable Long orderId){
        orderService.cancel(orderId, CancelReason.USER_CANCEL);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("취소 성공"));
    }

}
