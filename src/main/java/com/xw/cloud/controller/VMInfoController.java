package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xw.cloud.Utils.CommentResp;
import com.xw.cloud.bean.Ipaddr;
import com.xw.cloud.bean.VMInfo2;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.mapper.IpaddrMapper;
import com.xw.cloud.mapper.VmMapper;
import com.xw.cloud.service.LibvirtService;
import com.xw.cloud.service.VmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "虚拟机信息", description = "提供虚拟机信息")
@CrossOrigin
@Controller
@RequestMapping("/VMInfo")
public class VMInfoController {

    @Autowired
    private VmService vmService;

    @Resource
    private VmMapper vmMapper;

    @Resource
    private IpaddrMapper ipaddrMapper;

    @Resource(name = "libvirtService")
    private LibvirtService libvirtService;

    @ApiOperation(value = "修改status", notes = "根据ip修改status")
    @ResponseBody
    @RequestMapping("/updateStatus")
    public CommentResp updateStatus(@RequestParam("vmip") String ip,@RequestParam("status") int status) {
        VMInfo2 vmInfo2 = new VMInfo2();
        vmInfo2.setStatus(status);
        QueryWrapper<VMInfo2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip);
        vmMapper.update(vmInfo2,queryWrapper);
        return new CommentResp(true, null,"修改成功");
    }

    @ApiOperation(value = "获取物理机映射端口", notes = "通过指定的虚拟机ip和端口获取物理机映射端口")
    @ResponseBody
    @RequestMapping("/getHostPort")
    public CommentResp getPort(@RequestParam("vmip") String ip,@RequestParam("vmport") int vmport) {
        int[] arrayPort = {8000, 8085, 8086, 7051, 7052, 7053};
        int index = -1;

        for (int i = 0; i < 6; i++) {
            if (arrayPort[i] == vmport) {
                index = i;
                break;
            }
        }
        QueryWrapper<VMInfo2> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ip", ip);
        VMInfo2 vminfo = vmMapper.selectOne(queryWrapper);

//        Ipaddr ipaddr = ipaddrMapper.selectById(vminfo.getServerip());


        Map<String, Object> entry = new HashMap<>();
        entry.put("serverip", vminfo.getServerip());
        if(ip.equals("10.0.8.2")){
            entry.put("hostport", vmport);
        }
        else {
            entry.put("hostport", vminfo.getHostport()+index);
        }
//        entry.put("realip", ipaddr.getRealip());
        System.out.println(entry);
        return new CommentResp(true, entry,"");
    }


    //通过id得到用户信息
    @RequestMapping(value = "/getVMInfo/{name}", method = RequestMethod.GET)
    public String getUser(@PathVariable String name){
        return vmMapper.selectById(name).toString();
    }
    //通过id删除用户信息
    @RequestMapping(value = "/delete/{name}",method = RequestMethod.DELETE)
    @ResponseBody
    public CommentResp delete(@PathVariable("name") String name){
        System.out.println(name);
        int result = vmMapper.deleteById(name);
        if(result >= 1){
            return new CommentResp(true, null,"删除成功");
        }else{
            return new CommentResp(false, null,"删除失败");
        }
    }
    //更改用户信息
    @RequestMapping(value = "/updateip/{serverip:.*}",method = RequestMethod.GET)
    @ResponseBody
    @OperationLogDesc(module = "虚拟机信息管理", events = "虚拟机信息更新")
    public CommentResp updateip(@PathVariable("serverip") String serverip) throws IOException {
        libvirtService.getallVMip(serverip);
            return new CommentResp(true, null,"更新成功");
    }

    //插入用户信息
    @RequestMapping(value = "/insert")
    @ResponseBody
    @OperationLogDesc(module = "虚拟机信息管理", events = "添加虚拟机信息")
    public CommentResp insert(@RequestBody VMInfo2 temp) {
        int result = vmMapper.insert(temp);
        if (result >= 1) {
            return new CommentResp(true, null,"插入成功");
        } else {
            return new CommentResp(false, null,"插入失败");
        }
    }

    @ApiOperation(value = "获取虚拟机信息列表", notes = "获取虚拟机信息列表")
    @RequestMapping(value = "/selectAll/{usetype}")
    @ResponseBody
    public CommentResp  listVMInfo(@PathVariable("usetype") String usetype){
        List<VMInfo2> tempList;
        if(usetype.equals("federal")){
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("usetype",usetype);
            tempList = vmMapper.selectList(queryWrapper);
            QueryWrapper queryWrapper2 = new QueryWrapper();
            queryWrapper2.eq("usetype","flbc");
            tempList.addAll(vmMapper.selectList(queryWrapper2));

        }
        else {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("usetype", usetype);
            tempList = vmMapper.selectList(queryWrapper);
        }
//        return new CommentResp(true, tempList,"");
        List<Map<String, Object>> modifiedList = new ArrayList<>();
        List<Integer> fixedNumbers = new ArrayList<>();
        fixedNumbers.add(8000);
        fixedNumbers.add(8085);
        fixedNumbers.add(8086);
        fixedNumbers.add(7051);
        fixedNumbers.add(7052);
        fixedNumbers.add(7053);

        Integer[] fixed2={8000,8085,8086,7051,7052,7053};

        for (VMInfo2 item : tempList) {
            if (item.getHostport() == null) {
                Map<String, Object> modifiedEntry = new HashMap<>();
                modifiedEntry.put("name", item.getName());
                modifiedEntry.put("ip", item.getIp());
                modifiedEntry.put("username", item.getUsername());
                modifiedEntry.put("passwd", item.getPasswd());
                modifiedEntry.put("serverip", item.getServerip());
                modifiedEntry.put("cpuNum", item.getCpuNum());
                modifiedEntry.put("memory", item.getMemory());
                modifiedEntry.put("hostport", null);
                modifiedEntry.put("status", item.getStatus());

                modifiedList.add(modifiedEntry);

            } else {

                int hostport = item.getHostport();
                Map<String, Object> modifiedEntry = new HashMap<>();
                modifiedEntry.put("name", item.getName());
                modifiedEntry.put("ip", item.getIp());
                modifiedEntry.put("username", item.getUsername());
                modifiedEntry.put("passwd", item.getPasswd());
                modifiedEntry.put("serverip", item.getServerip());
                modifiedEntry.put("cpuNum", item.getCpuNum());
                modifiedEntry.put("memory", item.getMemory());
                modifiedEntry.put("status", item.getStatus());
                List<Map<String, Object>> hostportList = new ArrayList<>();
                if(item.getIp().equals("10.0.8.2")){
                    for (int i = 0; i < fixedNumbers.size(); i++) {
                        Map<String, Object> hostportEntry = new HashMap<>();
                        hostportEntry.put(String.valueOf(fixedNumbers.get(i)), String.valueOf(fixed2[i]));
                        hostportList.add(hostportEntry);
                    }
                }
                else {
                    for (int i = 0; i < fixedNumbers.size(); i++) {
                        Map<String, Object> hostportEntry = new HashMap<>();
                        hostportEntry.put(String.valueOf(fixedNumbers.get(i)), String.valueOf(hostport + i));
                        hostportList.add(hostportEntry);
                    }
                }
                modifiedEntry.put("hostport", hostportList);
                modifiedList.add(modifiedEntry);
            }
        }
        return new CommentResp(true, modifiedList, "");
    }



}

