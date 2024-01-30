package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import lombok.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@Builder
@Getter
@ToString
@ApiModel(description = "存储池信息")
public class Storagepool {
    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "容量(GB)")
    private int capacity;

    @ApiModelProperty(value = "可用容量(GB)")
    private int available;

    @ApiModelProperty(value = "已用容量(GB)")
    private int allocation;

    @ApiModelProperty(value = "使用率(%)")
    private String usage;

    @ApiModelProperty(value = "状态")
    private String state;

    @ApiModelProperty(value = "描述XML")
    private String xml;
 }
