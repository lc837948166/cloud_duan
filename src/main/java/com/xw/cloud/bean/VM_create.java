package com.xw.cloud.bean;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 创建虚拟机请求信息
 */
@Data
@ApiModel(description = "创建虚拟机请求信息")
public class VM_create {
    @ApiModelProperty(value = "虚拟机名称")
    @JsonProperty(value = "name")
    private String name;

    @ApiModelProperty(value = "内存大小")
    @JsonProperty(value = "memory")
    private int memory;

    @ApiModelProperty(value = "CPU核数")
    @JsonProperty(value = "cpuNum")
    private int cpuNum;

    @ApiModelProperty(value = "操作系统类型")
    @JsonProperty(value = "OStype")
    private String OStype;

    @ApiModelProperty(value = "镜像名")
    @JsonProperty(value = "ImgName")
    private String ImgName;

    @ApiModelProperty(value = "网络类型")
    @JsonProperty(value = "nettype")
    private String NetType;
}
