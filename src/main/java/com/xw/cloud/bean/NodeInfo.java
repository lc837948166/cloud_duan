package com.xw.cloud.bean;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description  
 * @Author  Hunter
 * @Date 2023-12-02 
 */


@TableName("T_Cloud_Node")
public class NodeInfo implements Serializable {

	private static final long serialVersionUID =  8153717320003653421L;

	@TableId(type = IdType.AUTO,value = "ID")
	private Integer id;

	@TableField(value = "nodeName")
	private String nodeName;

	@TableField(value = "nodeIp")
	private String nodeIp;

	@TableField(value = "nodeStatus")
	private String nodeStatus;

	@TableField(value = "nodeLocation")
	private String nodeLocation;

	@TableField(value = "nodeType")
	private String nodeType;

	@TableField(value = "nodeConnectivity")
	private Integer nodeConnectivity;

	@TableField(value = "nodeUserName")
	private String nodeUserName;

	@TableField(value = "nodeUserPasswd")
	private String nodeUserPasswd;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	@ApiModelProperty("操作时间")
	@TableField(value = "nodeCreateTime")
	Date nodeCreateTime;

	@TableField(value = "nodeLon")
	private Double nodeLon;

	@TableField(value = "nodeLat")
	private Double nodeLat;


	@TableField(value = "IsSchedulable")
	private Integer IsSchedulable;

	@TableField(value = "ALIAS")
	private String alias;

	@TableField(value = "IN_IP")
	private String inIp;

	@TableField(value = "BANDWIDTH")
	private String bandwidth;

	public NodeInfo(Integer id, String nodeName, String nodeIp, String nodeStatus, String nodeLocation, String nodeType, Integer nodeConnectivity, String nodeUserName, String nodeUserPasswd, Date nodeCreateTime, Double nodeLon, Double nodeLat, Integer isSchedulable, String alias, String inIp, String bandwidth) {
		this.id = id;
		this.nodeName = nodeName;
		this.nodeIp = nodeIp;
		this.nodeStatus = nodeStatus;
		this.nodeLocation = nodeLocation;
		this.nodeType = nodeType;
		this.nodeConnectivity = nodeConnectivity;
		this.nodeUserName = nodeUserName;
		this.nodeUserPasswd = nodeUserPasswd;
		this.nodeCreateTime = nodeCreateTime;
		this.nodeLon = nodeLon;
		this.nodeLat = nodeLat;
		IsSchedulable = isSchedulable;
		this.alias = alias;
		this.inIp = inIp;
		this.bandwidth = bandwidth;
	}


	public NodeInfo( String nodeName, String nodeIp, String nodeStatus, String nodeLocation, String nodeType, Integer nodeConnectivity, String nodeUserName, String nodeUserPasswd, Date nodeCreateTime, Double nodeLon, Double nodeLat, String alias,  String inIp, String bandwidth) {
		this.nodeName = nodeName;
		this.nodeIp = nodeIp;
		this.nodeStatus = nodeStatus;
		this.nodeLocation = nodeLocation;
		this.nodeType = nodeType;
		this.nodeConnectivity = nodeConnectivity;
		this.nodeUserName = nodeUserName;
		this.nodeUserPasswd = nodeUserPasswd;
		this.nodeCreateTime = nodeCreateTime;
		this.nodeLon = nodeLon;
		this.nodeLat = nodeLat;
		this.alias = alias;
		this.inIp = inIp;
		this.bandwidth = bandwidth;
	}

	public NodeInfo() {
	}

	@Override
	public String toString() {
		return "NodeInfo{" +
				"id=" + id +
				", nodeName='" + nodeName + '\'' +
				", nodeIp='" + nodeIp + '\'' +
				", nodeStatus='" + nodeStatus + '\'' +
				", nodeLocation='" + nodeLocation + '\'' +
				", nodeType='" + nodeType + '\'' +
				", nodeConnectivity=" + nodeConnectivity +
				", nodeUserName='" + nodeUserName + '\'' +
				", nodeUserPasswd='" + nodeUserPasswd + '\'' +
				", nodeCreateTime=" + nodeCreateTime +
				", nodeLon=" + nodeLon +
				", nodeLat=" + nodeLat +
				", IsSchedulable=" + IsSchedulable +
				", alias='" + alias + '\'' +
				", inIp='" + inIp + '\'' +
				", bandwidth='" + bandwidth + '\'' +
				'}';
	}

	public Integer getIsSchedulable() {
		return IsSchedulable;
	}

	public void setIsSchedulable(Integer isSchedulable) {
		IsSchedulable = isSchedulable;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getNodeIp() {
		return nodeIp;
	}

	public void setNodeIp(String nodeIp) {
		this.nodeIp = nodeIp;
	}

	public String getNodeStatus() {
		return nodeStatus;
	}

	public void setNodeStatus(String nodeStatus) {
		this.nodeStatus = nodeStatus;
	}

	public String getNodeLocation() {
		return nodeLocation;
	}

	public void setNodeLocation(String nodeLocation) {
		this.nodeLocation = nodeLocation;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public Integer getNodeConnectivity() {
		return nodeConnectivity;
	}

	public void setNodeConnectivity(Integer nodeConnectivity) {
		this.nodeConnectivity = nodeConnectivity;
	}

	public String getNodeUserName() {
		return nodeUserName;
	}

	public void setNodeUserName(String nodeUserName) {
		this.nodeUserName = nodeUserName;
	}

	public String getNodeUserPasswd() {
		return nodeUserPasswd;
	}

	public void setNodeUserPasswd(String nodeUserPasswd) {
		this.nodeUserPasswd = nodeUserPasswd;
	}

	public Date getNodeCreateTime() {
		return nodeCreateTime;
	}

	public void setNodeCreateTime(Date nodeCreateTime) {
		this.nodeCreateTime = nodeCreateTime;
	}

	public Double getNodeLon() {
		return nodeLon;
	}

	public void setNodeLon(Double nodeLon) {
		this.nodeLon = nodeLon;
	}

	public Double getNodeLat() {
		return nodeLat;
	}

	public void setNodeLat(Double nodeLat) {
		this.nodeLat = nodeLat;
	}

	public String getInIp() {
		return inIp;
	}

	public void setInIp(String inIp) {
		this.inIp = inIp;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(String bandwidth) {
		this.bandwidth = bandwidth;
	}
}
