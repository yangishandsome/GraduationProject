package com.yxc.user.controller;

import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.AddressService;
import com.yxc.user.domain.dto.AddOrUpdateAddressDTO;
import com.yxc.user.domain.po.Address;
import com.yxc.user.domain.vo.GetAddressVO;
import com.yxc.user.domain.vo.GetDefaultAddressVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Resource
    private AddressService addressService;

    @GetMapping("/getAddress")
    public Result<List<GetAddressVO>> getAddress() {
        Long userId = UserContext.getUser().getUserId();
        if(userId == null) {
            return Result.error("用户不存在");
        }
        return addressService.getAddress(userId);
    }

    @GetMapping("getDefaultAddress")
    public Result<GetDefaultAddressVO> getDefaultAddress() {
        Long userId = UserContext.getUser().getUserId();
        if(userId == null) {
            return Result.error("用户不存在");
        }
        return addressService.getDefaultAddress(userId);
    }

    @PutMapping("/addAddress")
    private Result<Long> addAddress(@RequestBody AddOrUpdateAddressDTO addOrUpdateAddressDTO) {
        return addressService.addAddress(addOrUpdateAddressDTO);
    }

    @PostMapping("/updateById")
    private Result<Long> updateById(@RequestBody AddOrUpdateAddressDTO addOrUpdateAddressDTO) {
        return addressService.updateById(addOrUpdateAddressDTO);
    }

    @DeleteMapping("/deleteById/{id}")
    private Result<Boolean> deleteById(@PathVariable(value = "id") Long id) {
        Address address = addressService.getById(id);
        if(address.getIsDefault() == 1) {
            return Result.error("默认地址无法删除");
        }
        return Result.ok(addressService.removeById(id));
    }

}
