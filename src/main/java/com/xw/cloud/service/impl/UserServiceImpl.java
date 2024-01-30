package com.xw.cloud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.UserInfo;
import com.xw.cloud.mapper.UserMapper;
import com.xw.cloud.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService{



    @Override
    public boolean login(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UserName", userInfo.getUserName())
                    .eq("Passwd ", userInfo.getPasswd())
                    .eq("UserGroupId", userInfo.getUserGroupId());
        UserInfo user = getOne(queryWrapper);
        return user != null;
    }

    @Override
    public boolean delete(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UserName", userInfo.getUserName())
                .eq("Passwd ", userInfo.getPasswd())
                .eq("UserGroupId", userInfo.getUserGroupId());
        boolean result = remove(queryWrapper);
        return result;
    }


    @Override
    public boolean register(UserInfo userInfo) {
        UserInfo user = new UserInfo();
        user.setUserName(userInfo.getUserName());
        user.setPasswd(userInfo.getPasswd());
        user.setUserGroupId(userInfo.getUserGroupId());
        boolean result = save(user);
        return result;
    }

    @Override
    public boolean logout() {

        return true;
    }
}
