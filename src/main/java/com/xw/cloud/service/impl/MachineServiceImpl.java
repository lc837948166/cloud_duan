package com.xw.cloud.service.impl;


import com.xw.cloud.Utils.HardWareUtil;
import com.xw.cloud.bean.*;
import com.xw.cloud.service.MachineService;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(value = "MachineService")
public class MachineServiceImpl implements MachineService {

    @SneakyThrows
    public MachineInfo getMachineInfo(){
        return HardWareUtil.getMachineInfo();
    }

    @Override
    public CpuInfo getCpuInfo() {
        return HardWareUtil.getCpuInfo();
    }

    @Override
    public MemoryInfo getMemInfo() {
        return HardWareUtil.getMemoryInfo(SizeEnum.GB);
    }

    @Override
    public List<SysFile> getSysFiles() {
        return HardWareUtil.getSysFiles();
    }

}
