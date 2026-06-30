package com.mishi.platform.module.address.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mishi.platform.common.exception.BusinessException;
import com.mishi.platform.module.address.dto.AddressDTO;
import com.mishi.platform.module.address.entity.Address;
import com.mishi.platform.module.address.mapper.AddressMapper;
import com.mishi.platform.module.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listByUser(Long userId) {
        return lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime)
                .list();
    }

    @Override
    @Transactional
    public Address add(Long userId, AddressDTO dto) {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setContactName(dto.getContactName());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());

        // 如果是首个地址，自动设为默认
        long count = lambdaQuery().eq(Address::getUserId, userId).count();
        addr.setIsDefault(count == 0 ? 1 : 0);
        addr.setCreateTime(LocalDateTime.now());
        save(addr);
        return addr;
    }

    @Override
    @Transactional
    public Address edit(Long id, Long userId, AddressDTO dto) {
        Address addr = lambdaQuery()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId).one();
        if (addr == null) {
            throw new BusinessException("地址不存在");
        }
        addr.setContactName(dto.getContactName());
        addr.setPhone(dto.getPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setUpdateTime(LocalDateTime.now());
        updateById(addr);
        return addr;
    }

    @Override
    @Transactional
    public void remove(Long id, Long userId) {
        Address addr = lambdaQuery()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId).one();
        if (addr == null) {
            throw new BusinessException("地址不存在");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        // 先取消其他默认
        lambdaUpdate()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0)
                .update();
        // 设置新默认
        lambdaUpdate()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 1)
                .update();
    }
}
