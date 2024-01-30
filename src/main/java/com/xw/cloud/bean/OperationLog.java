package com.xw.cloud.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * <p>
 * 操作日志表
 * </p>
 *
 */
@TableName("T_Cloud_Log_OperationLog")
@ApiModel(value = "OperationLog对象", description = "操作日志表")
public class OperationLog implements Serializable,Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * 实现 Cloneable 克隆拷贝
     * 创建一个 默认 对象，用于作为克隆的源数据
     */
    private static final OperationLog log = new OperationLog();

    /**
     * 获取克隆对象, 避免new的方式创建
     * @return {@link OperationLog}
     */
    public static OperationLog getInstance(){
        try {
            return log.clone();
        } catch (CloneNotSupportedException e) {
            return new OperationLog();
        }
    }


    /**
     * 重写克隆方法
     * @return {@link OperationLog}
     */
    public OperationLog clone() throws CloneNotSupportedException {
        return (OperationLog) super.clone();
    }

    /**
     * 私有化构造函数，不允许 new
     */
    private OperationLog(){
    }

    @TableId(type = IdType.AUTO,value = "ID")
    private Integer ID;



    @ApiModelProperty("操作模块")
    @TableField(value = "OperationModule")
    private String OperationModule;

    @ApiModelProperty("具体操作事件")
    @TableField(value = "OperationEvents")
    private String OperationEvents;

    @ApiModelProperty("操作Url")
    @TableField(value = "OperationUrl")
    private String OperationUrl;

    @ApiModelProperty("操作附带数据")
    @TableField(value = "OperationData")
    private String OperationData;

    @ApiModelProperty("操作是否正常，1正常操作， 0 操作异常")
    @TableField(value = "OperationStatus")
    private Boolean OperationStatus;

    @ApiModelProperty("操作结果")
    @TableField(value = "OperationResult")
    private String OperationResult;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @TableField(value = "AddTime")
    @ApiModelProperty("操作时间")
    private Date AddTime;

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getOperationModule() {
        return OperationModule;
    }

    public void setOperationModule(String operationModule) {
        OperationModule = operationModule;
    }

    public String getOperationEvents() {
        return OperationEvents;
    }

    public void setOperationEvents(String operationEvents) {
        OperationEvents = operationEvents;
    }

    public String getOperationUrl() {
        return OperationUrl;
    }

    public void setOperationUrl(String operationUrl) {
        OperationUrl = operationUrl;
    }

    public String getOperationData() {
        return OperationData;
    }

    public void setOperationData(String operationData) {
        OperationData = operationData;
    }

    public Boolean getOperationStatus() {
        return OperationStatus;
    }

    public void setOperationStatus(Boolean operationStatus) {
        OperationStatus = operationStatus;
    }

    public String getOperationResult() {
        return OperationResult;
    }

    public void setOperationResult(String operationResult) {
        OperationResult = operationResult;
    }

    public Date getAddTime() {
        return AddTime;
    }

    public void setAddTime(Date addTime) {
        AddTime = addTime;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "ID=" + ID +
                ", OperationModule='" + OperationModule + '\'' +
                ", OperationEvents='" + OperationEvents + '\'' +
                ", OperationUrl='" + OperationUrl + '\'' +
                ", OperationData='" + OperationData + '\'' +
                ", OperationStatus=" + OperationStatus +
                ", OperationResult='" + OperationResult + '\'' +
                ", AddTime=" + AddTime +
                '}';
    }
}

