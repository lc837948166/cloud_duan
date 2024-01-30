package com.xw.cloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.cloud.bean.VMInfo2;

import java.util.Map;

public interface VmService extends IService<VMInfo2> {
    /**
     * 查询虚拟机经纬度
     *
     * @param ip
     * @return
     */
    Map<String, Object> queryLatAndLon(String ip);
}
