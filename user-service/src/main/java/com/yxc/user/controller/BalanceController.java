package com.yxc.user.controller;

import com.yxc.common.domain.PageQuery;
import com.yxc.common.domain.PageVO;
import com.yxc.common.domain.Result;
import com.yxc.user.Service.BalanceService;
import com.yxc.user.domain.dto.AddBalanceDTO;
import com.yxc.user.domain.dto.DeductBalanceDTO;
import com.yxc.user.domain.po.BalanceRecords;
import com.yxc.user.domain.vo.AddBalanceVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    @Resource
    private BalanceService balanceService;

    @PostMapping("/addBalance")
    private Result<AddBalanceVO> addBalance(@RequestBody AddBalanceDTO addBalanceDTO) {
        return balanceService.addBalance(addBalanceDTO);
    }

    @GetMapping("/getBalanceRecords")
    private Result<PageVO<BalanceRecords>> getBalanceRecords(PageQuery pageQuery) {
        return balanceService.getBalanceRecords(pageQuery);
    }

    /**
     * OpenFeign接口
     */
    @PutMapping("/deduct")
    Result<?> deductBalance(@RequestBody DeductBalanceDTO deductBalanceDTO) {
        return balanceService.deductBalance(deductBalanceDTO);
    }

}
