package com.yxc.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.user.domain.dto.AddBalanceDTO;
import com.yxc.user.domain.dto.DeductBalanceDTO;
import com.yxc.user.domain.dto.ReturnBalanceDTO;
import com.yxc.user.domain.po.BalanceRecords;
import com.yxc.user.domain.vo.AddBalanceVO;

public interface BalanceService extends IService<BalanceRecords> {
    Result<AddBalanceVO> addBalance(AddBalanceDTO addBalanceDTO);

    Result<PageVO<BalanceRecords>> getBalanceRecords(PageQuery pageQuery);

    Result<?> deductBalance(DeductBalanceDTO deductBalanceDTO);

    Result<?> returnBalance(ReturnBalanceDTO returnBalanceDTO);
}
