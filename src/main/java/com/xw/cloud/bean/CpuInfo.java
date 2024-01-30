package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/**
 * CPU相关信息
 *
 */
@ApiModel(description = "CPU相关信息") // 描述整个类
@Data
@Getter
@ToString
public class CpuInfo {

    @ApiModelProperty(value = "核心数", example = "4") // 描述每个属性
    private int cpuNum;

    @ApiModelProperty(value = "CPU总的使用率", example = "50.5")
    private double total;

    @ApiModelProperty(value = "CPU系统使用率", example = "20.3")
    private double sys;

    @ApiModelProperty(value = "CPU用户使用率", example = "30.2")
    private double used;

    @ApiModelProperty(value = "CPU当前等待率", example = "5.0")
    private double wait;

    @ApiModelProperty(value = "CPU当前空闲率", example = "24.0")
    private double free;

}
