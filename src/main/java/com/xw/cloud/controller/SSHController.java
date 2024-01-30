package com.xw.cloud.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
@Api(tags = "SSH 管理", description = "提供 SSH 页面的访问")
@Controller
public class SSHController {
    @ApiOperation(value = "Web SSH 页面", notes = "返回 Web SSH 交互页面")
    @RequestMapping("/sshpage")
    public String webSSHpage(){
        return "webssh";
    }
}
