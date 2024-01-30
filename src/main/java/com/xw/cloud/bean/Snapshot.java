package com.xw.cloud.bean;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@ApiModel(description = "快照信息")
public class Snapshot {
    @ApiModelProperty(value = "快照名称")
    private String name;

    @ApiModelProperty(value = "创建时间")
    private String creationTime;

    @ApiModelProperty(value = "状态")
    private String state;
}
