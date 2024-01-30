package com.xw.cloud.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@TableName("T_CLOUDNETWORK_TASKS")
@ApiModel(value = "任务对象", description = "任务对象")
public class Task {

    /**
     * 私有化构造函数，不允许 new
     */
     public Task(){
    }

    @TableId(type = IdType.AUTO,value = "TASK_ID")
     Integer TASK_ID;


    @TableField(value = "TYPE_ID")
     Integer  TYPE_ID;


    @TableField(value = "TASK_NAME")
     String  TASK_NAME;


    @TableField(value = "TASK_DESCRIPTION")
     String TASK_DESCRIPTION;


    @TableField(value = "TASK_ATTRIBUTES_VALUES")
    String  TASK_ATTRIBUTES_VALUES ;

    @TableField(value = "STATUS")
    Integer  STATUS ;

    public Integer getTASK_ID() {
        return TASK_ID;
    }

    public void setTASK_ID(Integer TASK_ID) {
        this.TASK_ID = TASK_ID;
    }

    public Integer getTYPE_ID() {
        return TYPE_ID;
    }

    public void setTYPE_ID(Integer TYPE_ID) {
        this.TYPE_ID = TYPE_ID;
    }

    public String getTASK_NAME() {
        return TASK_NAME;
    }

    public void setTASK_NAME(String TASK_NAME) {
        this.TASK_NAME = TASK_NAME;
    }

    public String getTASK_DESCRIPTION() {
        return TASK_DESCRIPTION;
    }

    public void setTASK_DESCRIPTION(String TASK_DESCRIPTION) {
        this.TASK_DESCRIPTION = TASK_DESCRIPTION;
    }

    public String getTASK_ATTRIBUTES_VALUES() {
        return TASK_ATTRIBUTES_VALUES;
    }

    public void setTASK_ATTRIBUTES_VALUES(String TASK_ATTRIBUTES_VALUES) {
        this.TASK_ATTRIBUTES_VALUES = TASK_ATTRIBUTES_VALUES;
    }

    public Integer getSTATUS() {
        return STATUS;
    }

    public void setSTATUS(Integer STATUS) {
        this.STATUS = STATUS;
    }

    @Override
    public String toString() {
        return "Task{" +
                "TASK_ID=" + TASK_ID +
                ", TYPE_ID=" + TYPE_ID +
                ", TASK_NAME='" + TASK_NAME + '\'' +
                ", TASK_DESCRIPTION='" + TASK_DESCRIPTION + '\'' +
                ", TASK_ATTRIBUTES_VALUES='" + TASK_ATTRIBUTES_VALUES + '\'' +
                ", STATUS=" + STATUS +
                '}';
    }
}
