package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xw.cloud.Utils.CommentResp;
import com.xw.cloud.Utils.SftpUtils;
import com.xw.cloud.bean.*;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.mapper.VmMapper;
import com.xw.cloud.service.LibvirtService;
import com.xw.cloud.service.VmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.libvirt.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "虚拟化资源管理", description = "管理虚拟机、快照和存储池等虚拟化资源")
@CrossOrigin
@Controller
public class LibvirtController {

    @Resource(name = "libvirtService")
    private LibvirtService libvirtService;

    @Autowired
    private VmService vmService;

    @Resource
    private VmMapper vmMapper;



    @ApiOperation(value = "获取虚拟机列表", notes = "列出所有的虚拟机")
    @ResponseBody
    @GetMapping("/getVMList")
    @OperationLogDesc(module = "虚拟机管理", events = "获取虚拟机列表")
    public List<Virtual> getVMList() {
        return libvirtService.getVirtualList();
    }

    @ApiOperation(value = "获取虚拟机指标列表", notes = "列出所有的虚拟机指标")
    @ResponseBody
    @GetMapping("/getVMIndexList")
    @OperationLogDesc(module = "虚拟机管理", events = "获取虚拟机指标列表")
    public List<Virtual> getVMIndexList() {
        return libvirtService.getIndexList();
    }

    @ApiOperation(value = "获取虚拟机指标列表", notes = "列出虚拟机指标")
    @ResponseBody
    @GetMapping("/getVMIndex/{ip:.*}")
    public CommentResp getVMIndex(@PathVariable("ip") String ip) {
        QueryWrapper<VMInfo2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip);
        VMInfo2 vminfo = vmMapper.selectOne(queryWrapper);
        if(vminfo==null)return new CommentResp(false, 201, "当前ip不存在");

        Virtual virtual = libvirtService.getIndex(vminfo.getName(),vminfo.getUpBandWidth(),vminfo.getDownBandWidth());

        Map<String, Object> node = vmService.queryLatAndLon(ip);
        Map<String, Object> data = new HashMap<>();
        data.put("coordinate", node);
        data.put("virtual", virtual);

        return new CommentResp(true, data, "查询成功");
    }

//        return libvirtService.getIndex(ip);
//    }

    @ApiOperation(value = "开启/关闭网络", notes = "根据提供的网络状态开启或关闭网络")
    @RequestMapping("/openOrCloseNetWork")
    @OperationLogDesc(module = "虚拟机管理", events = "开启/关闭网络")
    public String openOrCloseNetWork(@RequestParam("netState") String netState) {
        if ("on".equals(netState)) libvirtService.closeNetWork();
        if ("off".equals(netState)) libvirtService.openNetWork();
        return "redirect:main";
    }

    @ApiOperation(value = "启动虚拟机", notes = "根据虚拟机名称启动虚拟机")
    @SneakyThrows
    @RequestMapping("/initiate/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "启动虚拟机")
    public CommentResp initiateVirtual(@PathVariable("name") String name) {
        libvirtService.initiateDomainByName(name);
        return new CommentResp(true, null,"启动成功");
    }

    @ApiOperation(value = "更改虚拟机配置", notes = "根据虚拟机名称更改虚拟机配置")
    @SneakyThrows
    @RequestMapping("/changeVM/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "更改虚拟机配置")
    public CommentResp changeVM(@PathVariable("name") String name,@RequestParam("cpuNum") int cpu,@RequestParam("memory") int mem) {
        libvirtService.shutdownDomainByName(name);
        Thread.sleep(5000);
        libvirtService.changeVMByName(name,cpu,mem);
        return new CommentResp(true, null,"更改虚拟机配置成功");
    }

    @ApiOperation(value = "挂起虚拟机", notes = "根据虚拟机名称挂起虚拟机")
    @SneakyThrows
    @RequestMapping("/suspended/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "挂起虚拟机")
    public CommentResp suspendedVirtual(@PathVariable("name") String name) {
        libvirtService.suspendedDomainName(name);
        return new CommentResp(true, null,"挂起成功");
    }

    @ApiOperation(value = "恢复虚拟机", notes = "根据虚拟机名称恢复虚拟机")
    @SneakyThrows
    @RequestMapping("/resume/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "恢复虚拟机")
    public CommentResp resumeVirtual(@PathVariable("name") String name) {
        libvirtService.resumeDomainByName(name);
        return new CommentResp(true, null,"还原成功");
    }

    @ApiOperation(value = "保存虚拟机", notes = "根据虚拟机名称保存虚拟机")
    @SneakyThrows
    @RequestMapping("/save")
    @OperationLogDesc(module = "虚拟机管理", events = "保存虚拟机")
    public String saveVirtual(@RequestParam("name") String name) {
        libvirtService.saveDomainByName(name);
        return "redirect:main";
    }

    @ApiOperation(value = "还原虚拟机", notes = "根据虚拟机名称还原虚拟机")
    @SneakyThrows
    @RequestMapping("/restore")
    @OperationLogDesc(module = "虚拟机管理", events = "还原虚拟机")
    public String restoreVirtual(@RequestParam("name") String name) {
        libvirtService.restoreDomainByName(name);
        return "redirect:main";
    }

    @ApiOperation(value = "关闭虚拟机", notes = "正常关闭指定的虚拟机")
    @SneakyThrows
    @RequestMapping("/shutdown/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "关闭虚拟机")
    public CommentResp shutdownVirtual(@PathVariable("name") String name) {
        libvirtService.shutdownDomainByName(name);
        return new CommentResp(true, null,"关机成功");
    }

    @ApiOperation(value = "强制关闭虚拟机", notes = "强制关闭指定的虚拟机")
    @SneakyThrows
    @RequestMapping("/shutdownMust/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "强制关闭虚拟机")
    public CommentResp shutdownMustVirtual(@PathVariable("name") String name) {
        libvirtService.shutdownMustDomainByName(name);
        return new CommentResp(true, null,"强行关机成功");
    }

    @ApiOperation(value = "重启虚拟机", notes = "重启指定的虚拟机")
    @SneakyThrows
    @RequestMapping("/reboot/{name}")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "重启虚拟机")
    public CommentResp rebootVirtual(@PathVariable("name") String name) {
        libvirtService.rebootDomainByName(name);
        return new CommentResp(true, null,"正在重启");
    }

    @ApiOperation(value = "删除虚拟机", notes = "删除指定的虚拟机和其关联的镜像文件")
    @RequestMapping(value = "/delete/{name}",method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "虚拟机管理", events = "删除虚拟机")
    public CommentResp deleteVirtual(@PathVariable("name") String name) {
        boolean flag=libvirtService.deletePort(name);
        if (flag){libvirtService.deleteDomainByName(name);}
        libvirtService.deleteImgFile(name);

        return new CommentResp(true, null,name+".qcow2删除成功");
    }

    @ApiOperation(value = "跳转至添加虚拟机页面", notes = "返回添加虚拟机的界面")
    @RequestMapping("/toAddVirtual")
    @ResponseBody
    //没啥用，不用看
    public String toAddVirtual(@RequestParam("name") String name,@RequestParam("ip") String ip) {

            VMInfo2 vmInfo2 = new VMInfo2();
            vmInfo2.setName(name);
            String data= SftpUtils.getexecon1(ip,"cat /proc/net/dev | awk '{i++; if(i>2){print $1}}' | sed 's/^[\\t]*//g' | sed 's/[:]*$//g'");
            String[] lines = data.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String str = lines[i];
            if (str.charAt(0) == 'e') {
                System.out.println("Index: " + i + ", String: " + str);
            }
        }

            System.out.println(lines[0]);
            return data;

    }

    @ApiOperation(value = "添加虚拟机", notes = "根据提供的信息添加新的虚拟机")
    @ResponseBody
    @RequestMapping("/addVirtual")
    @OperationLogDesc(module = "虚拟机管理", events = "添加虚拟机")
    public CommentResp addVirtual(@RequestParam("ImgName") String ImgName, @RequestParam("name") String name,
                             @RequestParam("memory") int memory, @RequestParam("cpuNum") int cpuNum,
                             @RequestParam(value = "OStype", defaultValue = "X86") String OStype,
                                  @RequestParam("nettype") String NetType,
                                  @RequestParam("serverip") String serverip,
                                  @RequestParam(value = "usetype", required = false) String usetype,
                                  @RequestParam(value = "bandwidth", required = false) Integer bandwidth
                                  ) throws InterruptedException {
        QueryWrapper<VMInfo2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        long count = vmMapper.selectCount(queryWrapper);
        if(count>0)return new CommentResp(false, null,"与现有虚拟机名重复");
        VM_create vmc = new VM_create();
        vmc.setName(name);
        vmc.setMemory(memory);
        vmc.setCpuNum(cpuNum);
        vmc.setOStype(OStype);
        vmc.setImgName(ImgName);
        vmc.setNetType(NetType);
        libvirtService.addImgFile(vmc.getName(),ImgName);
        libvirtService.addDomainByName(vmc,serverip);
        libvirtService.addport(name);
        if(usetype!=null&&!usetype.isEmpty())
        {
            VMInfo2 vm = new VMInfo2();
            vm.setName(name);
            vm.setUsetype(usetype);
            vmMapper.updateById(vm);
        }
        if(bandwidth!=null) {
            if(bandwidth==0)bandwidth=1000;
            VMInfo2 vm = new VMInfo2();
            vm.setName(name);
            vm.setUpBandWidth(bandwidth);
            vm.setDownBandWidth(bandwidth);
            vmMapper.updateById(vm);
        }

        return new CommentResp(true, null,"创建虚拟机"+name+"成功");
    }
}
