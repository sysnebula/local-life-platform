package com.localife.platform.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.localife.platform.module.user.dto.MerchantLoginDTO;
import com.localife.platform.module.user.dto.MerchantRegisterDTO;
import com.localife.platform.module.user.dto.UserLoginDTO;
import com.localife.platform.module.user.dto.WxLoginDTO;
import com.localife.platform.module.user.entity.User;
import com.localife.platform.module.user.vo.UserVO;

public interface UserService extends IService<User> {

    UserVO loginByPhone(UserLoginDTO dto);

    UserVO loginByWx(WxLoginDTO dto);

    UserVO loginByPassword(MerchantLoginDTO dto);

    UserVO register(MerchantRegisterDTO dto);

    UserVO getCurrentUser(Long userId);

    void updateProfile(Long userId, String nickName, String icon);
}
