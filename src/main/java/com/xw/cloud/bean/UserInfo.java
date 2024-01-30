package com.xw.cloud.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "USER信息")
@TableName("T_Cloud_User")
public class UserInfo implements Serializable {


    @TableId(type = IdType.AUTO, value = "ID")
    private Long id;

    @ApiModelProperty(value = "User名称", example = "admin")
    @TableField(value = "UserName")
    private String userName;

    @ApiModelProperty(value = "密码", example = "123456")
    @TableField(value = "Passwd")
    private String passwd;

    @ApiModelProperty(value = "用户组id", example = "1")
    @TableField(value = "UserGroupId ")
    private Integer userGroupId ;






}
