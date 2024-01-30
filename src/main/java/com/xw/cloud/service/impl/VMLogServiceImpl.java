package com.xw.cloud.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xw.cloud.bean.VMLog;
import com.xw.cloud.mapper.VMLogMapper;
import com.xw.cloud.service.VMLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class VMLogServiceImpl extends ServiceImpl<VMLogMapper, VMLog> implements VMLogService {
    @Autowired
    VMLogMapper vmLogMapper;

    @Override
    public List<String> getVmName() {
        return vmLogMapper.getVmName();
    }
}
