package com.xw.cloud.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(description = "PV信息")
@TableName("T_Cloud_PV")
public class PvInfo implements Serializable {


    @TableId(type = IdType.AUTO, value = "ID")
    private Long id;

    @ApiModelProperty(value = "PV名称", example = "my-pv")
    @TableField(value = "pvName")
    private String pvName;

    @ApiModelProperty(value = "PV路径", example = "/mnt/data")
    @TableField(value = "pvPath")
    private String pvPath;

    @ApiModelProperty(value = "PV数量", example = "10")
    @TableField(value = "pvQuantity")
    private String pvQuantity;

    @ApiModelProperty(value = "PV访问模式", example = "ReadWriteOnce")
    @TableField(value = "pvAccessMode")
    private String pvAccessMode;

    @ApiModelProperty(value = "PV节点名", example = "master1")
    @TableField(value = "pvNodeName")
    private String pvNodeName;

    @ApiModelProperty(value = "节点ID", example = "1")
    @TableField(value = "PvNodeId")
    private Integer pvNodeId;

    public PvInfo() {
    }

    public PvInfo(String pvName, String pvPath, String pvQuantity, String pvAccessMode, String pvNodeName, Integer pvNodeId) {
        this.pvName = pvName;
        this.pvPath = pvPath;
        this.pvQuantity = pvQuantity;
        this.pvAccessMode = pvAccessMode;
        this.pvNodeName = pvNodeName;
        this.pvNodeId = pvNodeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvName) {
        this.pvName = pvName;
    }

    public String getPvPath() {
        return pvPath;
    }

    public void setPvPath(String pvPath) {
        this.pvPath = pvPath;
    }

    public String getPvQuantity() {
        return pvQuantity;
    }

    public void setPvQuantity(String pvQuantity) {
        this.pvQuantity = pvQuantity;
    }

    public String getPvAccessMode() {
        return pvAccessMode;
    }

    public void setPvAccessMode(String pvAccessMode) {
        this.pvAccessMode = pvAccessMode;
    }

    public Integer getPvNodeId() {
        return pvNodeId;
    }

    public void setPvNodeId(Integer pvNodeId) {
        this.pvNodeId = pvNodeId;
    }

    public String getPvNodeName() {
        return pvNodeName;
    }

    public void setPvNodeName(String pvNodeName) {
        this.pvNodeName = pvNodeName;
    }

    @Override
    public String toString() {
        return "PvInfo{" +
                "id=" + id +
                ", pvName='" + pvName + '\'' +
                ", pvPath='" + pvPath + '\'' +
                ", pvQuantity='" + pvQuantity + '\'' +
                ", pvAccessMode='" + pvAccessMode + '\'' +
                ", pvNodeName='" + pvNodeName + '\'' +
                ", pvNodeId=" + pvNodeId +
                '}';
    }
}
