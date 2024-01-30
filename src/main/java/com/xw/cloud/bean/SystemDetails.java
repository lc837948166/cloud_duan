package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统相关信息
 */
@Data
@ApiModel(description = "系统相关信息")
public class SystemDetails {
    /**
     * 服务器名称
     */
    @ApiModelProperty(value = "服务器名称")
    private String computerName;

    /**
     * 服务器Ip
     */
    @ApiModelProperty(value = "服务器IP")
    private String computerIp;

    /**
     * 项目路径
     */
    @ApiModelProperty(value = "项目路径")
    private String userDir;

    /**
     * 操作系统
     */
    @ApiModelProperty(value = "操作系统")
    private String osName;

    /**
     * 系统架构
     */
    @ApiModelProperty(value = "系统架构")
    private String osArch;

    /**
     * 系统版本
     */
    @ApiModelProperty(value = "系统版本")
    private String osVersion;

    /**
     * 架构
     */
    @ApiModelProperty(value = "系统构建版本")
    private String osBuild;
}
