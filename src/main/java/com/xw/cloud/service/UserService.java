package com.xw.cloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.cloud.bean.UserInfo;

public interface UserService extends IService<UserInfo> {

    boolean register(UserInfo userInfo);

    boolean logout();

    boolean login(UserInfo userInfo);

    boolean delete(UserInfo userInfo);
}
