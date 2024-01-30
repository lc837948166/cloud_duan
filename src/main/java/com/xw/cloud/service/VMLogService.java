package com.xw.cloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xw.cloud.bean.VMLog;

import java.util.List;

public interface VMLogService extends IService<VMLog> {
    List<String>  getVmName();
}
