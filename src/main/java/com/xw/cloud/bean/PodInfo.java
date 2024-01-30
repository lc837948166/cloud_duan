package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@ApiModel(description = "Pod信息")
public class PodInfo {
    @ApiModelProperty(value = "Pod名称", example = "my-pod")
    private String podName;

    @ApiModelProperty(value = "Pod命名空间", example = "my-namespace")
    private String podNamespace;

    @ApiModelProperty(value = "Pod所在节点名称", example = "node-1")
    private String podNodeName;

    @ApiModelProperty(value = "容器信息列表")
    private List<ContainerInfo> containerInfoList;

    // 省略getter和setter方法

    @Override
    public String toString() {
        return "PodInfo{" +
                "podName='" + podName + '\'' +
                ", podNamespace='" + podNamespace + '\'' +
                ", podNodeName='" + podNodeName + '\'' +
                ", containerInfoList=" + containerInfoList +
                '}';
    }
}

