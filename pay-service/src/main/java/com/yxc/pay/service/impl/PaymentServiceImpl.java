package com.yxc.pay.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.Result;
import com.yxc.pay.domain.dto.RechargeDTO;
import com.yxc.pay.domain.po.RechargeOrder;
import com.yxc.pay.domain.vo.RechargeVO;
import com.yxc.pay.mapper.PaymentMapper;
import com.yxc.pay.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, RechargeOrder> implements PaymentService {

    @Override
    public Result<RechargeVO> createRecharge(RechargeDTO rechargeDTO) {
        return null;
    }
}
