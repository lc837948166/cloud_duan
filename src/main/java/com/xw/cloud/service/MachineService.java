package com.xw.cloud.service;


import com.xw.cloud.bean.CpuInfo;
import com.xw.cloud.bean.MachineInfo;
import com.xw.cloud.bean.MemoryInfo;
import com.xw.cloud.bean.SysFile;

import java.util.List;

public interface MachineService {

    public MachineInfo getMachineInfo();
    public CpuInfo getCpuInfo();
    public MemoryInfo getMemInfo();
    public List<SysFile> getSysFiles();
}
