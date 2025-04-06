package com.yxc.user.Service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yxc.common.domain.Result;
import com.yxc.common.utils.UserContext;
import com.yxc.user.Service.AddressService;
import com.yxc.user.domain.dto.AddOrUpdateAddressDTO;
import com.yxc.user.domain.po.Address;
import com.yxc.user.domain.vo.GetAddressVO;
import com.yxc.user.mapper.AddressMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public Result<Long> addAddress(AddOrUpdateAddressDTO addOrUpdateAddressDTO) {
        Long userId = UserContext.getUser();
        Long count = lambdaQuery().eq(Address::getUserId, userId).count();
        if(count >= 10) {
            return Result.error("您的收货地址已达到最大数量");
        }
        Address address = getAddress(addOrUpdateAddressDTO);
        if (address.getIsDefault() == 1) {
            lambdaUpdate()
                    .set( Address::getIsDefault, 0)
                    .eq(Address::getUserId, userId)
                    .update();
        }
        save(address);
        return Result.ok(1L);
    }

    @Override
    public Result<List<GetAddressVO>> getAddress(Long userId) {
        List<Address> addressList = lambdaQuery().eq(Address::getUserId, userId)
                .orderBy(true, false, Address::getIsDefault)
                .orderBy(true, false, Address::getCreateAt)
                .list();
        List<GetAddressVO> voList = new ArrayList<>();
        for (int i = 0; i < addressList.size(); i++) {
            GetAddressVO vo = BeanUtil.copyProperties(addressList.get(i), GetAddressVO.class);
            vo.setIsDefault(addressList.get(i).getIsDefault() == 1);
            vo.setEditing(false);
            List<String> selectedOptions = new ArrayList<>();
            if (StrUtil.isNotEmpty(vo.getProvince())) {
                selectedOptions.add(vo.getProvince());
            }
            selectedOptions.add(vo.getCity());
            selectedOptions.add(vo.getDistrict());
            vo.setSelectedOptions(selectedOptions);
            voList.add(vo);
        }
        return Result.ok(voList);
    }

    @Override
    public Result<Long> updateById(AddOrUpdateAddressDTO addOrUpdateAddressDTO) {
        List<String> locationInfo = addOrUpdateAddressDTO.getLocationInfo();
        Address address = getAddress(addOrUpdateAddressDTO);
        updateById(address);
        return Result.ok(1L);
    }

    private static Address getAddress(AddOrUpdateAddressDTO addOrUpdateAddressDTO) {
        List<String> locationInfo = addOrUpdateAddressDTO.getLocationInfo();
        Long userId = UserContext.getUser();
        int size = locationInfo.size();
        Address address = new Address();
        address.setId(addOrUpdateAddressDTO.getId());
        address.setDistrict(locationInfo.get(size - 1));
        address.setCity(locationInfo.get(size - 2));
        address.setProvince(size == 3 ? locationInfo.get(0) : null);
        address.setDetail(addOrUpdateAddressDTO.getDetail());
        address.setName(addOrUpdateAddressDTO.getName());
        address.setUserId(userId);
        address.setPhone(addOrUpdateAddressDTO.getPhone());
        address.setIsDefault(addOrUpdateAddressDTO.getIsDefault() ? 1 : 0);
        return address;
    }
}
