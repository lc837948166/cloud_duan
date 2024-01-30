package com.xw.cloud.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description  
 * @Author  Hunter
 * @Date 2023-12-02 
 */


@TableName("T_Cloud_Constructions")
public class ConstructionInfo implements Serializable {

	private static final long serialVersionUID =  8153717320003653421L;

	@TableId(type = IdType.AUTO,value = "ID")
	private Integer ID;

	@TableField(value = "TaskName")
	private String TaskName;

	@TableField(value = "VmName")
	private String VmName;

	@TableField(value = "ImgName")
	private String ImgName;

	@TableField(value = "OSType")
	private String OSType;

	@TableField(value = "NetType")
	private String NetType;

	@TableField(value = "CPUNum")
	private Integer CPUNum;

	@TableField(value = "Memory")
	private Integer Memory;

	@TableField(value = "Port")
	private Integer Port;

	@TableField(value = "Disk")
	private Integer Disk;

	@TableField(value = "ServerIp")
	private String ServerIp;

	@TableField(value = "FileName")
	private String FileName;

	@TableField(value = "Cmd")
	private String Cmd;

	@TableField(value = "OperationOrder")
	private Integer OperationOrder;

	@TableField(value = "OperationStatus")
	private Integer OperationStatus;

	@TableField(value = "TaskType")
	private String TaskType;
	public Integer getID() {
		return ID;
	}

	public void setID(Integer ID) {
		this.ID = ID;
	}

	public String getTaskName() {
		return TaskName;
	}

	public void setTaskName(String taskName) {
		TaskName = taskName;
	}

	public String getVmName() {
		return VmName;
	}

	public void setVmName(String vmName) {
		VmName = vmName;
	}

	public String getImgName() {
		return ImgName;
	}

	public void setImgName(String imgName) {
		ImgName = imgName;
	}

	public String getOSType() {
		return OSType;
	}

	public void setOSType(String OSType) {
		this.OSType = OSType;
	}

	public String getNetType() {
		return NetType;
	}

	public void setNetType(String netType) {
		NetType = netType;
	}

	public Integer getCPUNum() {
		return CPUNum;
	}

	public void setCPUNum(Integer CPUNum) {
		this.CPUNum = CPUNum;
	}

	public Integer getMemory() {
		return Memory;
	}

	public void setMemory(Integer memory) {
		Memory = memory;
	}

	public Integer getPort() {
		return Port;
	}

	public void setPort(Integer port) {
		Port = port;
	}

	public Integer getDisk() {
		return Disk;
	}

	public void setDisk(Integer disk) {
		Disk = disk;
	}

	public String getServerIp() {
		return ServerIp;
	}

	public void setServerIp(String serverIp) {
		ServerIp = serverIp;
	}

	public String getCmd() {
		return Cmd;
	}

	public void setCmd(String cmd) {
		Cmd = cmd;
	}

	public Integer getOperationOrder() {
		return OperationOrder;
	}

	public void setOperationOrder(Integer operationOrder) {
		OperationOrder = operationOrder;
	}

	public Integer getOperationStatus() {
		return OperationStatus;
	}

	public void setOperationStatus(Integer operationStatus) {
		OperationStatus = operationStatus;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String getTaskType() {
		return TaskType;
	}

	public void setTaskType(String taskType) {
		TaskType = taskType;
	}

	@Override
	public String toString() {
		return "ConstructionInfo{" +
				"ID=" + ID +
				", TaskName='" + TaskName + '\'' +
				", VmName='" + VmName + '\'' +
				", ImgName='" + ImgName + '\'' +
				", OSType='" + OSType + '\'' +
				", NetType='" + NetType + '\'' +
				", CPUNum=" + CPUNum +
				", Memory=" + Memory +
				", Port=" + Port +
				", Disk=" + Disk +
				", ServerIp='" + ServerIp + '\'' +
				", FileName='" + FileName + '\'' +
				", Cmd='" + Cmd + '\'' +
				", OperationOrder=" + OperationOrder +
				", OperationStatus=" + OperationStatus +
				", TaskType=" + TaskType +
				'}';
	}
}
