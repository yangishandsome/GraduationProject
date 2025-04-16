package com.yxc.pay.controller;

import com.yxc.common.domain.Result;
import com.yxc.pay.domain.dto.RechargeDTO;
import com.yxc.pay.domain.vo.RechargeVO;
import com.yxc.pay.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
@Slf4j
@RestController
@RequestMapping("/pay")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/recharge")
    private Result<RechargeVO> createRecharge(@RequestBody RechargeDTO rechargeDTO) {
        return paymentService.createRecharge(rechargeDTO);
    }
}
