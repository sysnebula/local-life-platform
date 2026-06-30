package com.mishi.platform.module.address.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mishi.platform.module.address.dto.AddressDTO;
import com.mishi.platform.module.address.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> listByUser(Long userId);
    Address add(Long userId, AddressDTO dto);
    Address edit(Long id, Long userId, AddressDTO dto);
    void remove(Long id, Long userId);
    void setDefault(Long id, Long userId);
}
