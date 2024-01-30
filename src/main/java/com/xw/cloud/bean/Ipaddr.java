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
 * ip信息
 */
@Data
@TableName(value = "T_CLOUD_IP")
@ApiModel(description = "IP信息")
public class Ipaddr {
    @TableId(type = IdType.AUTO)
    @JsonProperty(value = "serverip")
    @ApiModelProperty(value = "zerotier为节点分配的ip")
    private String serverip;
    @JsonProperty(value = "realip")
    @ApiModelProperty(value = "虚拟机真实ip")
    private String realip;
}
