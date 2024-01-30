package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@ApiModel(description = "Libvirt连接信息")
public class LibvirtConnect {
    @ApiModelProperty(notes = "URL")
    private String url;

    @ApiModelProperty(notes = "主机名")
    private String hostName;

    @ApiModelProperty(notes = "Libvirt版本")
    private long libVirVersion;

    @ApiModelProperty(notes = "Hypervisor版本")
    private String hypervisorVersion;
}
