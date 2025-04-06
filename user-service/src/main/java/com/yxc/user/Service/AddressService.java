package com.yxc.user.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yxc.common.domain.Result;
import com.yxc.user.domain.dto.AddOrUpdateAddressDTO;
import com.yxc.user.domain.po.Address;
import com.yxc.user.domain.vo.GetAddressVO;

import java.util.List;

public interface AddressService extends IService<Address> {
    Result<Long> addAddress(AddOrUpdateAddressDTO addOrUpdateAddressDTO);

    Result<List<GetAddressVO>> getAddress(Long userId);

    Result<Long> updateById(AddOrUpdateAddressDTO addOrUpdateAddressDTO);
}
