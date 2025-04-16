package com.yxc.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.Result;
import com.yxc.pay.domain.dto.RechargeDTO;
import com.yxc.pay.domain.po.RechargeOrder;
import com.yxc.pay.domain.vo.RechargeVO;

public interface PaymentService extends IService<RechargeOrder> {
    Result<RechargeVO> createRecharge(RechargeDTO rechargeDTO);
}
