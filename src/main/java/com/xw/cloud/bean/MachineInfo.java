package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Data
@Getter
@ToString
@Builder
@ApiModel(description = "机器相关信息")
public class MachineInfo {
    @ApiModelProperty(notes = "CPU信息")
    private CpuInfo cpuInfo;

    @ApiModelProperty(notes = "JVM相关信息")
    private JvmInfo jvmInfo;

    @ApiModelProperty(notes = "内存相关信息")
    private MemoryInfo memoryInfo;

    @ApiModelProperty(notes = "系统详细信息")
    private SystemDetails systeminfo;

    @ApiModelProperty(notes = "系统文件列表")
    private List<SysFile> sysFiles;
}