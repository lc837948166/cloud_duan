package com.xw.cloud.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//@Getter
//@ToString
//@Setter
//@AllArgsConstructor

/**
 * 虚拟机信息
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName(value = "T_CLOUD_VM")
@ApiModel(description = "虚拟机信息")
public class VMInfo2 {
    @TableId(type = IdType.AUTO)
    @JsonProperty(value = "name")
    @ApiModelProperty(value = "虚拟机名称")
    private String name;
    @JsonProperty(value = "ip")
    @ApiModelProperty(value = "虚拟机ip")
    private String ip;
    @JsonProperty(value = "username")
    @ApiModelProperty(value = "用户名")
    private String username;
    @JsonProperty(value = "passwd")
    @ApiModelProperty(value = "密码")
    private String passwd;
    @JsonProperty(value = "serverip")
    @ApiModelProperty(value = "虚拟机所在服务器ip")
    private String serverip;
    @JsonProperty(value = "cpuNum")
    @ApiModelProperty(value = "虚拟机最大cpu")
    private Integer cpuNum;
    @JsonProperty(value = "memory")
    @ApiModelProperty(value = "虚拟机最大内存")
    private Integer memory;
    @JsonProperty(value = "hostport")
    @ApiModelProperty(value = "宿主机起始映射端口")
    private Integer hostport;
    @JsonProperty(value = "status")
    @ApiModelProperty(value = "状态")
    private Integer status;
    @JsonProperty(value = "usetype")
    @ApiModelProperty(value = "federal or null")
    private String usetype;
    @JsonProperty(value = "upBandWidth")
    @ApiModelProperty(value = "upBandWidth")
    private Integer upBandWidth;
    @JsonProperty(value = "downBandWidth")
    @ApiModelProperty(value = "downBandWidth")
    private Integer downBandWidth;
    @JsonProperty(value = "nic")
    @ApiModelProperty(value = "nic")
    private String nic;
}
