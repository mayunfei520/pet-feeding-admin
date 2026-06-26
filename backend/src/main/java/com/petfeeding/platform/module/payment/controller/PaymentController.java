package com.petfeeding.platform.module.payment.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.payment.entity.Payment;
import com.petfeeding.platform.module.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 支付控制器
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付管理", description = "订单支付记录管理")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/all")
    @Operation(summary = "所有支付记录")
    public R<List<Payment>> listAll() {
        return R.ok(paymentService.list());
    }
}
