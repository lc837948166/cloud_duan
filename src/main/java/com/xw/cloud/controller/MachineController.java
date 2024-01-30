package com.xw.cloud.controller;

import com.xw.cloud.Utils.CommentResp;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.MachineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/Machine")
@Api(tags = {"物理机管理"},description = "用于处理物理机信息的接口")
public class MachineController {
    @Resource
    private MachineService machineService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/getMachineInfo")
    @ApiOperation(value= "查询物理机信息", notes= "用于查询物理机信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "查询物理机信息")
    public CommentResp getMachineInfo() {
        return new CommentResp(true, machineService.getMachineInfo(), "");
    }

    @GetMapping("/getCpuInfo")
    @ApiOperation(value= "查询CPU信息", notes= "用于查询CPU信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "查询CPU信息")
    public CommentResp getCpuInfo() {
        return new CommentResp(true, machineService.getCpuInfo(), "");
    }

    @GetMapping("/getMemInfo")
    @ApiOperation(value= "查询内存信息", notes= "用于查询内存信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "查询内存信息")
    public CommentResp getMemInfo() {
        return new CommentResp(true, machineService.getMemInfo(), "");
    }

    @GetMapping("/SysFiles")
    @ApiOperation(value= "查询内存信息", notes= "用于查询内存信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "查询内存信息")
    public CommentResp getSysfiles() {
        return new CommentResp(true, machineService.getSysFiles(), "");
    }

    @GetMapping("/getMachineInfoByIP")
    @ApiOperation(value= "根据ip查询物理机信息", notes= "用于根据ip查询物理机信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "根据ip查询物理机信息")
    public Object getMachineInfoByIP(@RequestParam(value = "ip") String ip) {
        String url = "http://" + ip + ":8080/Machine/getMachineInfo";
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/getCpuInfoByIP")
    @ApiOperation(value= "根据ip查询CPU信息", notes= "用于根据ip查询CPU信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "根据ip查询CPU信息")
    public Object getCpuInfoByIP(@RequestParam(value = "ip") String ip) {
        String url = "http://" + ip + ":8080/Machine/getCpuInfo";
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/getMemInfoByIP")
    @ApiOperation(value= "根据ip查询内存信息", notes= "用于根据ip查询内存信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "根据ip查询内存信息")
    public Object getMemInfoByIP(@RequestParam(value = "ip") String ip) {
        String url = "http://" + ip + ":8080/Machine/getMemInfo";
        return restTemplate.getForObject(url, Object.class);
    }

    @GetMapping("/getSysFilesByIP")
    @ApiOperation(value= "根据ip查询文件系统信息", notes= "用于根据ip查询文件系统信息的接口")
    @OperationLogDesc(module = "物理机管理", events = "根据ip查询文件系统信息")
    public Object getSysFilesByIP(@RequestParam(value = "ip") String ip) {
        String url = "http://" + ip + ":8080/Machine/getSysFiles";
        return restTemplate.getForObject(url, Object.class);
    }


}


