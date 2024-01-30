package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "镜像信息") // 描述整个类
public class ImageInfo {

    @ApiModelProperty(value = "镜像名称", example = "example-image")
    private String imageName;

    @ApiModelProperty(value = "镜像标签", example = "latest")
    private String imageTag;

    @ApiModelProperty(value = "镜像ID", example = "1234567890abcdef")
    private String imageId;

    @ApiModelProperty(value = "镜像大小", example = "500MB")
    private String imageSize;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }
}
