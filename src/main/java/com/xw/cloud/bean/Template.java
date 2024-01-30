package com.xw.cloud.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

//@Getter
//@ToString
//@Setter
//@AllArgsConstructor
/**
 * 虚拟机模板信息
 */
@Data
@TableName(value = "T_CLOUD_VM_TEMPLATE")
@ApiModel(description = "虚拟机模板信息")
public class Template {
    @TableId(type = IdType.AUTO)
    @JsonProperty(value = "id")
    @ApiModelProperty(value = "模板ID")
    private int id;
    @JsonProperty(value = "name")
    @ApiModelProperty(value = "模板名称")
    private String name;
    @JsonProperty(value = "memory")
    @ApiModelProperty(value = "内存大小")
    private long memory;
    @JsonProperty(value = "cpuNum")
    @ApiModelProperty(value = "CPU核数")
    private int cpuNum;
    @JsonProperty(value = "OStype")
    @ApiModelProperty(value = "操作系统类型")
    private String OStype;
    @JsonProperty(value = "ImgName")
    @ApiModelProperty(value = "镜像名")
    private String ImgName;
    @JsonProperty(value = "nettype")
    @ApiModelProperty(value = "网络类型")
    private String NetType;
}
