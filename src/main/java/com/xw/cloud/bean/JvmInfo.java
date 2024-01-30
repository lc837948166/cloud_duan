package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Getter
@ToString
@ApiModel(description = "JVM相关信息")
public class JvmInfo {
    @ApiModelProperty(notes = "当前JVM占用的内存总数(G)")
    private double total;

    @ApiModelProperty(notes = "JVM最大可内存总数(G)")
    private double max;

    @ApiModelProperty(notes = "JVM空闲内存(M)")
    private double free;

    @ApiModelProperty(notes = "JDK版本")
    private String version;

    @ApiModelProperty(notes = "JDK路径")
    private String home;
}