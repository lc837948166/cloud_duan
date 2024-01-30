package com.xw.cloud.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@ApiModel(description = "镜像实体类")
public class ImgFile {
    @ApiModelProperty(notes = "镜像名称")
    private String name;

    @ApiModelProperty(notes = "镜像大小")
    private String size;
    @ApiModelProperty(notes = "文件名后缀")
    private String end;
}
