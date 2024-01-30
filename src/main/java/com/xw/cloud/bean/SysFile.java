package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统文件相关信息
 */
@Data
@ApiModel(description = "系统文件相关信息")
public class SysFile {
    /**
     * 盘符路径
     */
    @ApiModelProperty(value = "盘符路径")
    private String dirName;

    /**
     * 盘符类型
     */
    @ApiModelProperty(value = "盘符类型")
    private String sysTypeName;

    /**
     * 文件类型
     */
    @ApiModelProperty(value = "文件类型")
    private String typeName;

    /**
     * 总大小
     */
    @ApiModelProperty(value = "总大小")
    private String total;

    /**
     * 剩余大小
     */
    @ApiModelProperty(value = "剩余大小")
    private String free;

    /**
     * 已经使用量
     */
    @ApiModelProperty(value = "已使用量")
    private String used;

    /**
     * 资源的使用率
     */
    @ApiModelProperty(value = "资源使用率")
    private double usage;
}
