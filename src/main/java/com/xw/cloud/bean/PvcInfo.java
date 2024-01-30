package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@ApiModel(value = "持久化存储卷声明信息", description = "用于描述持久化存储卷声明的信息")
public class PvcInfo {

    @ApiModelProperty(value = "持久化存储卷声明名称",example = "my-pvc")
    private String pvcName;

    @ApiModelProperty(value = "持久化存储卷声明命名空间", example = "my-namespace")
    private String pvcNamespace;

    @ApiModelProperty(value = "持久化存储卷声明数量", example = "3")
    private String pvcQuantity;

    // 省略getter和setter方法

    @Override
    public String toString() {
        return "PvcInfo{" +
                "pvcName='" + pvcName + '\'' +
                ", pvcNamespace='" + pvcNamespace + '\'' +
                ", pvcQuantity='" + pvcQuantity + '\'' +
                '}';
    }
}
