package com.xw.cloud.bean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(description = "请求信息")
public class RequestInfo {
    @ApiModelProperty(value = "Pod信息")
    private PodInfo podInfo;

    @ApiModelProperty(value = "持久卷声明信息")
    private PvcInfo pvcInfo;

    @ApiModelProperty(value = "容器信息")
    private ContainerInfo containerInfo;

    @ApiModelProperty(value = "镜像信息")
    private ImageInfo imageInfo;

    @ApiModelProperty(value = "物理卷信息")
    private PvInfo pvInfo;

    @ApiModelProperty(value = "虚拟机信息")
    private VmInfo vmInfo;

    public RequestInfo() {
    }

    public RequestInfo(PodInfo podInfo, PvcInfo pvcInfo, ContainerInfo containerInfo, ImageInfo imageInfo, PvInfo pvInfo, VmInfo vmInfo) {
        this.podInfo = podInfo;
        this.pvcInfo = pvcInfo;
        this.containerInfo = containerInfo;
        this.imageInfo = imageInfo;
        this.pvInfo = pvInfo;
        this.vmInfo = vmInfo;
    }

    public PodInfo getPodInfo() {
        return podInfo;
    }

    public void setPodInfo(PodInfo podInfo) {
        this.podInfo = podInfo;
    }

    public PvcInfo getPvcInfo() {
        return pvcInfo;
    }

    public void setPvcInfo(PvcInfo pvcInfo) {
        this.pvcInfo = pvcInfo;
    }

    public ContainerInfo getContainerInfo() {
        return containerInfo;
    }

    public void setContainerInfo(ContainerInfo containerInfo) {
        this.containerInfo = containerInfo;
    }

    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    public PvInfo getPvInfo() {
        return pvInfo;
    }

    public void setPvInfo(PvInfo pvInfo) {
        this.pvInfo = pvInfo;
    }

    public VmInfo getVmInfo() {
        return vmInfo;
    }

    public void setVmInfo(VmInfo vmInfo) {
        this.vmInfo = vmInfo;
    }

    @Override
    public String toString() {
        return "RequestInfo{" +
                "podInfo=" + podInfo +
                ", pvcInfo=" + pvcInfo +
                ", containerInfo=" + containerInfo +
                ", imageInfo=" + imageInfo +
                ", pvInfo=" + pvInfo +
                ", vmInfo=" + vmInfo +
                '}';
    }
}
