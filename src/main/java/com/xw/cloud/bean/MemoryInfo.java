package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Getter
@ToString
@ApiModel(description = "内存相关信息")
public class MemoryInfo {
    @ApiModelProperty(value = "内存总量", example = "8192")
    private double total;

    @ApiModelProperty(value = "已用内存", example = "4096")
    private double used;

    @ApiModelProperty(value = "剩余内存", example = "4096")
    private double free;
}