package com.xw.cloud.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@TableName("T_Cloud_Log_VMLog")
@ApiModel(value = "VMLog对象", description = "VM日志表")
public class VMLog {

    /**
     * 私有化构造函数，不允许 new
     */
     public VMLog(){
    }

    @TableId(type = IdType.AUTO,value = "ID")
     Integer ID;
    @ApiModelProperty("vm名")
    @TableField(value = "VmName")
     String VmName;

    @ApiModelProperty("VM日志内容")
    @TableField(value = "VmContent")
     String VmContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty("操作时间")
    @TableField(value = "AddTime")
     Date AddTime;
    @TableField(value = "DisplayContent")
    private String DisplayContent;

    @TableField(value = "NODEIP")
    private String NodeIp;
    public String getDisplayContent() {
        return DisplayContent;
    }

    public void setDisplayContent(String displayContent) {
        DisplayContent = displayContent;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }


    public String getVmName() {
        return VmName;
    }

    public void setVmName(String vmNames) {
        VmName = vmNames;
    }

    public String getVmContent() {
        return VmContent;
    }

    public void setVmContent(String vmContent) {
        VmContent = vmContent;
    }

    public Date getAddTime() {
        return AddTime;
    }

    public void setAddTime(Date addTime) {
        AddTime = addTime;
    }

    public String getNodeIp() {
        return NodeIp;
    }

    public void setNodeIp(String nodeIp) {
        NodeIp = nodeIp;
    }

    @Override
    public String toString() {
        return "VMLog{" +
                "ID=" + ID +
                ", VmName='" + VmName + '\'' +
                ", VmContent='" + VmContent + '\'' +
                ", AddTime=" + AddTime +
                ", DisplayContent='" + DisplayContent + '\'' +
                ", NodeIp='" + NodeIp + '\'' +
                '}';
    }
}
