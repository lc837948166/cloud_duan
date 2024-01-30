package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "容器信息") // 描述整个类
public class ContainerInfo {

    @ApiModelProperty(value = "容器名称", example = "myapp-container")
    private String containerName;

    @ApiModelProperty(value = "容器镜像", example = "myapp-image:latest")
    private String containerImage;

    @ApiModelProperty(value = "容器端口号", example = "8080")
    private int port;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerImage() {
        return containerImage;
    }

    public void setContainerImage(String containerImage) {
        this.containerImage = containerImage;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    // 省略 getter 和 setter 方法
}