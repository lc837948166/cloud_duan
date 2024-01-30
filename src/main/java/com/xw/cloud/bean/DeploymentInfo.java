package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "部署信息") // 描述整个类
public class DeploymentInfo {
    @ApiModelProperty(value = "部署名称", example = "my-deployment")
    private String deploymentName;

    @ApiModelProperty(value = "镜像", example = "example-image:latest")
    private String image;

    @ApiModelProperty(value = "容器端口号", example = "8080")
    private Integer containerPort;

    @ApiModelProperty(value = "服务端口号", example = "80")
    private Integer servicePort;

    @ApiModelProperty(value = "节点端口号", example = "30000")
    private Integer nodePort;

    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public Integer getServicePort() {
        return servicePort;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }
}
