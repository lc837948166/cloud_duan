package com.xw.cloud.bean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(description = "虚拟机信息")
public class VmInfo {

    @ApiModelProperty(value = "虚拟机IP地址")
    private String virtualMachineIp;

    @ApiModelProperty(value = "虚拟机用户名")
    private String userName;

    @ApiModelProperty(value = "虚拟机用户密码")
    private String userPassword;

    public String getVirtualMachineIp() {
        return virtualMachineIp;
    }

    public void setVirtualMachineIp(String virtualMachineIp) {
        this.virtualMachineIp = virtualMachineIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
