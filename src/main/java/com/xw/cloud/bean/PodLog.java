package com.xw.cloud.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@TableName("T_Cloud_Log_PodLog")
@ApiModel(value = "OperationLog对象", description = "操作日志表")
public class PodLog {

    /**
     * 私有化构造函数，不允许 new
     */
     public PodLog(){
    }

    @TableId(type = IdType.AUTO,value = "ID")
     Integer ID;

    @ApiModelProperty("pod名")
    @TableField(value = "PodName")
     String PodName;

    @ApiModelProperty("命令空间")
    @TableField(value = "Spaces")
     String Spaces;

    @ApiModelProperty("日志内容")
    @TableField(value = "PodContent")
     String PodContent;




    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @ApiModelProperty("操作时间")
    @TableField(value = "AddTime")
     Date AddTime;

    @TableField(value = "DisplayContent")
    private String DisplayContent;

    @TableField(value = "NODENAME")
    private String NodeName;
    public void setPodName(String podName) {
        PodName = podName;
    }

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

    public String getPodName() {
        return PodName;
    }

    public void setPodNames(String podName) {
        PodName = podName;
    }

    public String getSpaces() {
        return Spaces;
    }

    public void setSpaces(String spaces) {
        Spaces = spaces;
    }

    public String getPodContent() {
        return PodContent;
    }

    public void setPodContent(String podContent) {
        PodContent = podContent;
    }

    public Date getAddTime() {
        return AddTime;
    }

    public void setAddTime(Date addTime) {
        AddTime = addTime;
    }

    public String getNodeName() {
        return NodeName;
    }

    public void setNodeName(String nodeName) {
        NodeName = nodeName;
    }

    @Override
    public String toString() {
        return "PodLog{" +
                "ID=" + ID +
                ", PodName='" + PodName + '\'' +
                ", Spaces='" + Spaces + '\'' +
                ", PodContent='" + PodContent + '\'' +
                ", AddTime=" + AddTime +
                ", DisplayContent='" + DisplayContent + '\'' +
                ", NodeName='" + NodeName + '\'' +
                '}';
    }
}
