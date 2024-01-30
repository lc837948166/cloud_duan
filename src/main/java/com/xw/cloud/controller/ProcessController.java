package com.xw.cloud.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xw.cloud.Utils.*;

import com.xw.cloud.bean.ConstructionInfo;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.Task;
import com.xw.cloud.bean.VMInfo2;
import com.xw.cloud.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin
@Controller
@Api(tags = "任务管理", description = "执行任务")
public class ProcessController {

    @Autowired
    private ConstructionService constructionService;
    @Autowired
    private VmService vmService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private LibvirtService libvirtService;
    private final String sufixUrl = ":8081/api/ssh/execute2";
    private static List<Integer> taskList ;
    @Autowired
    private TaskService taskService;
    static {
        taskList = new LinkedList();
        taskList.add(33);
        taskList.add(34);
        taskList.add(35);
        taskList.add(39);
        taskList.add(41);
        taskList.add(42);
        taskList.add(43);
        taskList.add(44);
        taskList.add(45);
    }

/*    @RequestMapping(value = "/addVirtual", method = RequestMethod.POST)
    @ApiOperation(value = "创建虚拟机接口", notes = "创建虚拟机并上传镜像")
    @ResponseBody
    public void addVirtual(@RequestParam String taskName) throws IOException {
        ProcessUtils processUtils = new ProcessUtils();
        String filePath = "/opt/"+"taskLog.txt";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
        }catch (Exception e){
            e.printStackTrace();
        }
        List<Task> tasks = taskService.list();
        for(Task task: tasks){
            //记录中间任务执行失败情况
            if(task.getTYPE_ID()!=23){
                continue;
            }
            String task_attributes_values = task.getTASK_ATTRIBUTES_VALUES();
            ObjectMapper mapper = new ObjectMapper();
            TaskUtils taskUtils = null;
            System.out.println(task_attributes_values);
            try {
                taskUtils = mapper.readValue(task_attributes_values, TaskUtils.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                continue;
            }
            if((taskUtils.getTask_status() == 1 || taskUtils.getTask_status() == 5) && (taskUtils.getTask_executor() == 1 || taskUtils.getTask_executor() == 3)){
                System.out.println("开始执行任务"+task.getTASK_NAME());
                task.setSTATUS(2);
                taskUtils.setTask_status(2);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                bw.write("开始执行任务"+task.getTASK_NAME()+"\n");
                boolean flag = false;
                int vmNum = taskUtils.getVm_num();
                String mkcommand = "mkdir -p /etc/usr/xwfiles";
                if(vmNum <= 0) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    continue;
                }
                String vm_image_name = taskUtils.getVm_image_name();
                String vm_name = taskUtils.getVm_name();
                Integer memory = taskUtils.getMemory();
                Integer cpu_num = taskUtils.getCpu_num();
                String docker_image_name = taskUtils.getDocker_image_name();
                String[] com = taskUtils.getCmds().split(",");
                if(vm_image_name==null ||vm_name.equals("") || vm_name == null || vm_name.equals("") || memory== null || cpu_num == null || docker_image_name == null|| docker_image_name.equals("") || com == null || com.equals("")) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("缺少任务所需信息\n");
                    continue;
                }
                if(vm_image_name.equals("template.qcow2")){
                    vm_image_name = "template.qcow2";
                }
                String OStype = "X86";
                String nettype = "bridge";
                Integer is_all_pm = taskUtils.getIs_all_pm();
                List<NodeInfo>  nodes = null;
                if(is_all_pm == 1){
                    nodes = nodeService.list(new QueryWrapper<NodeInfo>().eq("IsSchedulable", "1"));
                }

                String ips = "";
                String[] ipip = taskUtils.getPm_ip().split(",");
                if(nodes == null){
                    nodes = new ArrayList<>();
                    for(String ip: ipip) {
                        NodeInfo node = nodeService.getOne(new QueryWrapper<NodeInfo>().eq("NODEIP",ip));
                        if(node == null){
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("端节点不存在");
                            flag = true;
                            break;
                        }
                        nodes.add(node);
                    }
                }
                if(!flag){
                    int cnt = 0;
                    for(NodeInfo node : nodes){
                        String Pmip = node.getNodeIp();
                        String url = "http://" + Pmip + sufixUrl;
                        //循环上传镜像
                        if(flag)
                            break;
                        //下发镜像文件
                        System.out.println("节点"+node.getNodeName()+"下发镜像文件");
                        bw.write("节点"+node.getNodeName()+"下发镜像文件\n");
                        String ans = null;
                        try {
                            ans = processUtils.dispenseImgByIP("39.98.124.97", docker_image_name, Pmip);
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点"+node.getNodeName()+"下发镜像文件失败\n");
                            flag = true;
                            taskService.updateById(task);
                            break;
                        }
                        if (ans.contains("200")) {
                            if (ans.contains("-1")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                bw.write("节点"+node.getNodeName()+"下发镜像文件失败\n");
                                taskService.updateById(task);
                                break;
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点"+node.getNodeName()+"下发镜像文件失败\n");
                            flag = true;
                            taskService.updateById(task);
                            break;
                        }
                        for(int i = 1; i <= vmNum;i++) {
                            String vmi_name = vm_name+"_"+cnt;
                            cnt++;
                            System.out.println("节点"+node.getNodeName()+"创建第"+i+"个虚拟机");

                            bw.write("节点"+node.getNodeName()+"创建第"+i+"个虚拟机\n");
                            //创建虚拟机
                            try {
                                ans = processUtils.createVM(vm_image_name,vmi_name,memory,cpu_num,OStype,nettype,Pmip);
                                if (ans.contains("200")) {
                                    if (ans.contains("重复")) {
                                        task.setSTATUS(5);
                                        taskUtils.setTask_status(5);
                                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                        flag = true;
                                        bw.write("创建失败\n");
                                        taskService.updateById(task);
                                        break;
                                    }else {
                                        QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                                        qw1.eq("name", vmi_name);
                                        VMInfo2 vm = vmService.getOne(qw1);
                                        if (vm.getIp() == null) {
                                            boolean ff = false;
                                            for (int j = 0; j < 8; ++j) {
                                                if (vmService.getOne(qw1).getIp() == null || vmService.getOne(qw1).getIp().isEmpty()) {
                                                    Thread.sleep(6000);
                                                    libvirtService.getallVMip(Pmip);
                                                } else {
                                                    ff = true;

                                                    if(ips.equals("")){
                                                        ips = vmService.getOne(qw1).getIp();
                                                    }else
                                                    {
                                                        System.out.println("节点"+node.getNodeName()+"第"+i+"个虚拟机获得IP:"+vmService.getOne(qw1).getIp());
                                                        ips +=","+vmService.getOne(qw1).getIp();
                                                    }

                                                    break;
                                                }
                                            }
                                            if(!ff) {
                                                task.setSTATUS(5);
                                                taskUtils.setTask_status(5);
                                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                                flag = true;
                                                taskService.updateById(task);
                                                break;
                                            }
                                        }else {
                                            System.out.println("第"+i+"个虚拟机获得IP:"+vmService.getOne(qw1).getIp());
                                            bw.write("第"+i+"个虚拟机获得IP:"+vmService.getOne(qw1).getIp()+"\n");
                                            if(ips.equals("")){
                                                ips = vmService.getOne(qw1).getIp();
                                            }else
                                            {
                                                ips +=","+vmService.getOne(qw1).getIp();
                                            }
                                        }
                                    }
                                } else {
                                    task.setSTATUS(5);
                                    bw.write("创建失败\n");
                                    taskUtils.setTask_status(5);
                                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                    flag = true;
                                    taskService.updateById(task);
                                    break;
                                }
                            } catch (Exception e) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                bw.write("创建失败\n");
                                taskService.updateById(task);
                                break;
                            }

                            bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令创建文件夹\n");
                            System.out.println("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令创建文件夹");
                            //执行命令创建文件夹
                            QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                            qw1.eq("name", vmi_name);
                            VMInfo2 vm = vmService.getOne(qw1);
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);

                            // 构建请求体
                            Map<String, Object> requestBody = new HashMap<>();
                            requestBody.put("host", vm.getIp());
                            requestBody.put("username", vm.getUsername());
                            requestBody.put("password", vm.getPasswd());
                            List<String> commands = Arrays.asList(
                                    mkcommand
                            );
                            requestBody.put("commands", commands);
                            System.out.println(commands);
                            // 发起请求
                            ResponseEntity<String> response = new RestTemplate().exchange(
                                    url,
                                    HttpMethod.POST,
                                    new HttpEntity<>(requestBody, headers),
                                    String.class
                            );
                            if (!response.toString().contains("\"exitStatus\":0")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令创建文件夹失败\n");
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                taskService.updateById(task);
                                break;
                            }
                            //上传文件到虚拟机

                            bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机上传文件到虚拟机\n");
                            System.out.println("节点"+node.getNodeName()+"第"+i+"个虚拟机上传文件到虚拟机");
                            try {
                                ans = processUtils.uploadDockerToVM(docker_image_name, vmi_name, Pmip, "39.98.124.97");
                            } catch (Exception e) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                taskService.updateById(task);
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机上传文件到虚拟机失败\n");
                                break;
                            }
                            System.out.println(ans);
                            if (ans.contains("200")) {
                                if (ans.contains("-1")) {
                                    task.setSTATUS(5);
                                    taskUtils.setTask_status(5);
                                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                    bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机上传文件到虚拟机失败\n");
                                    flag = true;
                                    taskService.updateById(task);
                                    break;
                                }
                            } else {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机上传文件到虚拟机失败\n");
                                taskService.updateById(task);
                                break;
                            }
                            bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机导入镜像\n");
                            System.out.println("节点"+node.getNodeName()+"第"+i+"个虚拟机导入镜像");
                            //导入镜像
                            try {
                                ans = processUtils.importImage(docker_image_name, vmi_name, Pmip);
                            } catch (Exception e) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                flag = true;
                                taskService.updateById(task);
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机导入镜像失败\n");
                                break;
                            }
                            System.out.println(ans);
                            if (ans.contains("200")) {
                                if (ans.contains("-1")) {
                                    task.setSTATUS(5);
                                    flag = true;
                                    taskUtils.setTask_status(5);
                                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                    bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机导入镜像失败\n");
                                    taskService.updateById(task);
                                    break;
                                }
                            } else {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机导入镜像失败\n");
                                flag = true;
                                taskService.updateById(task);
                                break;
                            }
                            bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令运行镜像\n");
                            System.out.println("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令运行镜像");
                            //执行命令运行镜像
                            HttpHeaders headers1 = new HttpHeaders();
                            headers1.setContentType(MediaType.APPLICATION_JSON);

                            // 构建请求体
                            Map<String, Object> requestBody1 = new HashMap<>();
                            requestBody1.put("host", vm.getIp());
                            requestBody1.put("username", vm.getUsername());
                            requestBody1.put("password", vm.getPasswd());
                            List<String> commands1 = Arrays.asList(
                                    com
                            );
                            requestBody1.put("commands", commands1);
                            System.out.println(commands);
                            // 发起请求
                            ResponseEntity<String> response1 = new RestTemplate().exchange(
                                    url,
                                    HttpMethod.POST,
                                    new HttpEntity<>(requestBody1, headers1),
                                    String.class
                            );
                            if (!response1.toString().contains("\"exitStatus\":0")) {
                                task.setSTATUS(5);
                                bw.write("节点"+node.getNodeName()+"第"+i+"个虚拟机执行命令运行镜像失败\n");
                                flag = true;
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                break;
                            }
                        }
                    }
                    if(flag == false){
                        task.setSTATUS(4);
                        bw.write("任务"+task.getTASK_NAME()+"成功结束\n");
                        bw.write("=====================================\n");
                        taskUtils.setVm_ip(ips);
                        taskUtils.setTask_status(4);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                    }
                }
            }
        }
        bw.close();
        System.out.println("任务结束");
    }*/


/*
    @RequestMapping(value = "/addVirtualByTaskID", method = RequestMethod.POST)
    @ApiOperation(value = "根据任务ID创建虚拟机接口", notes = "根据任务ID创建虚拟机并上传镜像")
    @ResponseBody
    public CommonResp addVirtualByTaskName(@RequestParam Integer taskID) throws IOException {
        ProcessUtils processUtils = new ProcessUtils();
        String filePath = "/opt/" + "taskLog.txt";
        String OStype = "X86";
        String nettype = "bridge";
        String ips = "";
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Task task = taskService.getOne(new QueryWrapper<Task>().eq("TASK_ID", taskID).eq("TYPE_ID", 23));
        if (task == null) {
            return new CommonResp(false, "", "任务不存在");
        }
        String task_attributes_values = task.getTASK_ATTRIBUTES_VALUES();
        ObjectMapper mapper = new ObjectMapper();
        TaskUtils taskUtils = null;
        System.out.println(task_attributes_values);
        try {
            taskUtils = mapper.readValue(task_attributes_values, TaskUtils.class);
        } catch (IOException e) {
            task.setSTATUS(5);
            taskUtils.setTask_status(5);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            return new CommonResp(false, "", "任务执行失败");
        }
        if ((taskUtils.getTask_status() == 2 || taskUtils.getTask_status() == 1 || taskUtils.getTask_status() == 5) && (taskUtils.getTask_executor() == 1 || taskUtils.getTask_executor() == 3)) {
            System.out.println("开始执行任务" + task.getTASK_NAME());
            bw.write("开始执行任务" + task.getTASK_NAME() +"，任务ID为"+taskID+ "\n");
            task.setSTATUS(2);
            taskUtils.setTask_status(2);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            int vmNum = taskUtils.getVm_num();
            String mkcommand = "mkdir -p /etc/usr/xwfiles";
            String vm_image_name = taskUtils.getVm_image_name();
            String vm_name = taskUtils.getVm_name();
            String memorys = taskUtils.getMemory();
            String cpu_nums = taskUtils.getCpu_num();
            String diskss = taskUtils.getDisk();
            String docker_image_name = taskUtils.getDocker_image_name();
            String[] com = taskUtils.getCmds().split(",");
            String[] ipip = taskUtils.getPm_ip().split(",");
            if (vm_image_name == null || vm_name.equals("")) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                bw.write("缺少虚拟机镜像名\n");
                bw.close();
                return new CommonResp(false, "", "缺少虚拟机镜像名");
            }
            if (docker_image_name == null || docker_image_name.equals("")) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                bw.write("缺少Docker镜像名\n");
                bw.close();
                return new CommonResp(false, "", "缺少Docker镜像名");
            }
            if (com == null || com.equals("")) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                bw.write("缺少执行命令\n");
                bw.close();
                return new CommonResp(false, "", "缺少执行命令");
            }
            Integer is_all_pm = taskUtils.getIs_all_pm();
            List<NodeInfo> nodes = null;
            if (is_all_pm == 1) {
                nodes = nodeService.list(new QueryWrapper<NodeInfo>().eq("ISSCHEDULABLE", "1"));
            }
            //处理节点集合nodes
            if (nodes == null) {
                nodes = new ArrayList<>();
                for (String ip : ipip) {
                    NodeInfo node = nodeService.getOne(new QueryWrapper<NodeInfo>().eq("NODEIP", ip));
                    if (node == null) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("节点IP不存在");
                        bw.close();
                        return new CommonResp(false, "", "节点IP不存在");
                    }
                    nodes.add(node);
                }
            }
            int node_number = nodes.size();
            //处理Disk memory,cpu
            Integer[] cpu = new Integer[vmNum];
            Integer[] memory = new Integer[vmNum];
            Integer[] disk = new Integer[vmNum];
            String[] cpu_split = cpu_nums.split(",");
            String[] memory_split = memorys.split(",");
            String[] disk_split = diskss.split(",");
//            处理cpu
            if (vmNum == 1) {
                if (cpu_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("CPU数输入有误\n");
                    bw.close();
                    return new CommonResp(false, "", "CPU数输入有误");
                }
                cpu[0] = (int)Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("CPU数输入有误");
                    bw.close();
                    return new CommonResp(false, "", "CPU数输入有误");
                } else if (cpu_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        cpu[i] = (int)Double.parseDouble(cpu_split[0]);
                    }
                } else if (cpu_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        cpu[i] = (int)(Double.parseDouble(cpu_split[i]));
                    }
                }
            }
            //处理memory
            if (vmNum == 1) {
                if (memory_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("内存数输入有误\n");
                    bw.close();
                    return new CommonResp(false, "", "内存数输入有误\n");
                }
                memory[0] = (int)Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("内存数输入有误");
                    bw.close();
                    return new CommonResp(false, "", "内存数输入有误");
                } else if (memory_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        memory[i] = (int)Double.parseDouble(memory_split[0]);
                    }
                } else if (memory_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        memory[i] = (int)Double.parseDouble(memory_split[i]);
                    }
                }
            }
            //处理disk
            if (vmNum == 1) {
                if (disk_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("磁盘数输入有误\n");
                    bw.close();
                    return new CommonResp(false, "", "磁盘数输入有误\n");
                }
                disk[0] = (int)Double.parseDouble(disk_split[0]);
            } else {
                if (disk_split.length != 1 && disk_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("磁盘数输入有误");
                    bw.close();
                    return new CommonResp(false, "", "磁盘数输入有误");
                } else if (disk_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        disk[i] = (int)Double.parseDouble(disk_split[0]);
                    }
                } else if (disk_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        disk[i] = (int)Double.parseDouble(disk_split[i]);
                    }
                }
            }
            //计算每个节点需要分配的虚拟机数
            int vn = vmNum / node_number;
            int vn_end = vmNum % node_number + vn;
            int cnt = 0;
            for (int i = 0; i < nodes.size(); i++) {
                NodeInfo node = nodes.get(i);
                if (i == nodes.size() - 1) {
                    vn = vn_end;
                }
                String Pmip = node.getNodeIp();
                String url = "http://" + Pmip + sufixUrl;
                //下发镜像文件
                System.out.println("节点" + node.getNodeName() + "下发镜像文件");
                bw.write("节点" + node.getNodeName() + "下发镜像文件\n");
                String ans = null;
                try {
                    ans = processUtils.dispenseImgByIP("39.98.124.97", docker_image_name, Pmip);
                    System.out.println(ans);
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                }
                if (ans.contains("200")) {
                    if (ans.contains("\"exitStatus\":-1") && !ans.contains("Command execution timed")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                }
                for (int j = 0; j < vn; j++) {
                    String vmi_name = vm_name + "_" + (cnt+1);
                    System.out.println("节点" + node.getNodeName() + "创建第" + (cnt+1) + "个虚拟机");
                    bw.write("节点" + node.getNodeName() + "创建第" + (cnt+1) + "个虚拟机"+vmi_name+"\n");
                    //创建虚拟机
                    try {
                        ans = processUtils.createVM(vm_image_name, vmi_name, memory[cnt], cpu[cnt], OStype, nettype, Pmip);
                        if (ans.contains("200")) {
                            if (ans.contains("重复")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("虚拟机名字重复，创建失败\n");
                                taskService.updateById(task);
                                bw.close();
                                return new CommonResp(false, "", "虚拟机名字重复，创建失败");
                            } else {
                                QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                                qw1.eq("name", vmi_name);
                                VMInfo2 vm = vmService.getOne(qw1);
                                if (vm.getIp() == null) {
                                    task.setSTATUS(5);
                                    taskUtils.setTask_status(5);
                                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                    bw.write("虚拟机未获得IP，创建失败\n");
                                    taskService.updateById(task);
                                    bw.close();
                                    return new CommonResp(false, "", "虚拟机未获得IP，创建失败\n");

                                } else {
                                    System.out.println("第" + (cnt+1) + "个虚拟机获得IP:" + vmService.getOne(qw1).getIp());
                                    bw.write("第" + (cnt+1) + "个虚拟机获得IP:" + vmService.getOne(qw1).getIp() + "\n");
                                    if (ips.equals("")) {
                                        ips = vmService.getOne(qw1).getIp();
                                    } else {
                                        ips += "," + vmService.getOne(qw1).getIp();
                                    }
                                }
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("虚拟机创建失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "虚拟机创建失败");
                        }
                    } catch (Exception e) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("虚拟机创建失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "虚拟机创建失败");
                    }
                    bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹");
                    //执行命令创建文件夹
                    QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                    qw1.eq("name", vmi_name);
                    VMInfo2 vm = vmService.getOne(qw1);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    // 构建请求体
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("host", vm.getIp());
                    requestBody.put("username", vm.getUsername());
                    requestBody.put("password", vm.getPasswd());
                    List<String> commands = Arrays.asList(
                            mkcommand
                    );
                    requestBody.put("commands", commands);
                    System.out.println(commands);
                    // 发起请求
                    ResponseEntity<String> response = new RestTemplate().exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody, headers),
                            String.class
                    );
                    if (!response.toString().contains("\"exitStatus\":0")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹失败\n");
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹失败");
                    }
                    //上传文件到虚拟机

                    bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机");
                    try {
                        ans = processUtils.uploadDockerToVM(docker_image_name, vmi_name, Pmip, "39.98.124.97");
                    } catch (Exception e) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败\n");
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败");
                    }
                    System.out.println(ans);
                    if (ans.contains("200")) {
                        if (ans.contains("-1")) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败");
                        }
                    } else {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机上传文件到虚拟机失败");
                    }
                    bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像");
                    //导入镜像
                    try {
                        ans = processUtils.importImage(docker_image_name, vmi_name, Pmip);
                    } catch (Exception e) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                    }
                    System.out.println(ans);
                    if (ans.contains("200")) {
                        if (ans.contains("-1")) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                        }
                    } else {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机导入镜像失败\n");
                    }
                    bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令运行镜像\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令运行镜像");
                    //执行命令运行镜像
                    HttpHeaders headers1 = new HttpHeaders();
                    headers1.setContentType(MediaType.APPLICATION_JSON);

                    // 构建请求体
                    Map<String, Object> requestBody1 = new HashMap<>();
                    requestBody1.put("host", vm.getIp());
                    requestBody1.put("username", vm.getUsername());
                    requestBody1.put("password", vm.getPasswd());
                    List<String> commands1 = Arrays.asList(
                            com
                    );
                    requestBody1.put("commands", commands1);
                    System.out.println(commands1);
                    // 发起请求
                    ResponseEntity<String> response1 = new RestTemplate().exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody1, headers1),
                            String.class
                    );
                    if (!response1.toString().contains("\"exitStatus\":0")) {
                        task.setSTATUS(5);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令运行镜像失败\n");
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令运行镜像失败\n");
                    }
                    cnt++;
                }
            }

        }else {
            return new CommonResp(false,"","任务不存在");
        }
        task.setSTATUS(4);
        bw.write("任务" + task.getTASK_NAME() + "成功结束\n");
        bw.write("=====================================\n");
        bw.close();
        taskUtils.setVm_ip(ips);
        taskUtils.setTask_status(4);
        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
        taskService.updateById(task);
        return new CommonResp(true,"","任务执行成功");
}
*/


 /*   @RequestMapping(value = "/addVirtualByTaskID", method = RequestMethod.POST)
    @ApiOperation(value = "根据任务ID创建虚拟机接口", notes = "根据任务ID创建虚拟机并上传镜像")
    @ResponseBody
    public CommonResp addVirtualByTaskID(@RequestParam Integer taskID) throws IOException {
        ProcessUtils processUtils = new ProcessUtils();
        String filePath = "/opt/" + "taskLog.txt";
        String OStype = "X86";
        String nettype = "nat";
        String ans = null;
        String ips = "";
        BufferedWriter bw = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Task task = taskService.getOne(new QueryWrapper<Task>().eq("TASK_ID", taskID));
        if (task == null) {
            return new CommonResp(false, "", "任务不存在");
        }
        if(task.getTYPE_ID() == 23) {
            TaskUtils taskUtils = null;
            String task_attributes_values = task.getTASK_ATTRIBUTES_VALUES();
            System.out.println(task_attributes_values);
            try {
                taskUtils = mapper.readValue(task_attributes_values, TaskUtils.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                return new CommonResp(false, "", "任务执行失败");
            }
            if ((taskUtils.getTask_status() == 2 || taskUtils.getTask_status() == 1 || taskUtils.getTask_status() == 5) && (taskUtils.getTask_executor() == 1 || taskUtils.getTask_executor() == 3)) {
                System.out.println("开始执行任务" + task.getTASK_NAME());
                bw.write("开始执行任务" + task.getTASK_NAME() + "，任务ID为" + taskID + "\n");
                task.setSTATUS(2);
                taskUtils.setTask_status(2);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                int vmNum = taskUtils.getVm_num();
//            String mkcommand = "mkdir -p /etc/usr/xwfiles";
                String vm_image_name = taskUtils.getVm_image_name();
                String vm_name = taskUtils.getVm_name();
                String memorys = taskUtils.getMemory();
                String cpu_nums = taskUtils.getCpu_num();
                String diskss = taskUtils.getDisk();
                String docker_image_name = taskUtils.getDocker_image_name();
                String[] com = taskUtils.getCmds().split(",");
                String[] ipip = taskUtils.getPm_ip().split(",");
                if (vm_image_name == null || vm_name.equals("")) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("缺少虚拟机镜像名\n");
                    bw.close();
                    return new CommonResp(false, "", "缺少虚拟机镜像名");
                }
                if (vm_image_name.contains("centos")) {
                    vm_image_name = "centos.qcow2";
                }
                if (docker_image_name == null || docker_image_name.equals("")) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("缺少Docker镜像名\n");
                    bw.close();
                    return new CommonResp(false, "", "缺少Docker镜像名");
                }
                if (com == null || com.equals("")) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("缺少执行命令\n");
                    bw.close();
                    return new CommonResp(false, "", "缺少执行命令");
                }
                Integer is_all_pm = taskUtils.getIs_all_pm();
                List<NodeInfo> nodes = null;
                if (is_all_pm == 1) {
                    nodes = nodeService.list(new QueryWrapper<NodeInfo>().eq("ISSCHEDULABLE", "1"));
                }
                //处理节点集合nodes
                if (nodes == null) {
                    nodes = new ArrayList<>();
                    for (String ip : ipip) {
                        NodeInfo node = nodeService.getOne(new QueryWrapper<NodeInfo>().eq("NODEIP", ip));
                        if (node == null) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("节点IP不存在");
                            bw.close();
                            return new CommonResp(false, "", "节点IP不存在");
                        }
                        nodes.add(node);
                    }
                }
                int node_number = nodes.size();
                //处理Disk memory,cpu
                Integer[] cpu = new Integer[vmNum];
                Integer[] memory = new Integer[vmNum];
                Integer[] disk = new Integer[vmNum];
                String[] cpu_split = cpu_nums.split(",");
                String[] memory_split = memorys.split(",");
                String[] disk_split = diskss.split(",");
//            处理cpu
                if (vmNum == 1) {
                    if (cpu_split.length != 1) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("CPU数输入有误\n");
                        bw.close();
                        return new CommonResp(false, "", "CPU数输入有误");
                    }
                    cpu[0] = (int) Double.parseDouble(cpu_split[0]);
                } else {
                    if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("CPU数输入有误");
                        bw.close();
                        return new CommonResp(false, "", "CPU数输入有误");
                    } else if (cpu_split.length == 1) {
                        for (int i = 0; i < vmNum; i++) {
                            cpu[i] = (int) Double.parseDouble(cpu_split[0]);
                        }
                    } else if (cpu_split.length == vmNum) {
                        for (int i = 0; i < vmNum; i++) {
                            cpu[i] = (int) (Double.parseDouble(cpu_split[i]));
                        }
                    }
                }
                //处理memory
                if (vmNum == 1) {
                    if (memory_split.length != 1) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("内存数输入有误\n");
                        bw.close();
                        return new CommonResp(false, "", "内存数输入有误\n");
                    }
                    memory[0] = (int) Double.parseDouble(memory_split[0]);
                } else {
                    if (memory_split.length != 1 && memory_split.length != vmNum) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("内存数输入有误");
                        bw.close();
                        return new CommonResp(false, "", "内存数输入有误");
                    } else if (memory_split.length == 1) {
                        for (int i = 0; i < vmNum; i++) {
                            memory[i] = (int) Double.parseDouble(memory_split[0]);
                        }
                    } else if (memory_split.length == vmNum) {
                        for (int i = 0; i < vmNum; i++) {
                            memory[i] = (int) Double.parseDouble(memory_split[i]);
                        }
                    }
                }
                //处理disk
                if (vmNum == 1) {
                    if (disk_split.length != 1) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("磁盘数输入有误\n");
                        bw.close();
                        return new CommonResp(false, "", "磁盘数输入有误\n");
                    }
                    disk[0] = (int) Double.parseDouble(disk_split[0]);
                } else {
                    if (disk_split.length != 1 && disk_split.length != vmNum) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("磁盘数输入有误");
                        bw.close();
                        return new CommonResp(false, "", "磁盘数输入有误");
                    } else if (disk_split.length == 1) {
                        for (int i = 0; i < vmNum; i++) {
                            disk[i] = (int) Double.parseDouble(disk_split[0]);
                        }
                    } else if (disk_split.length == vmNum) {
                        for (int i = 0; i < vmNum; i++) {
                            disk[i] = (int) Double.parseDouble(disk_split[i]);
                        }
                    }
                }
                //计算每个节点需要分配的虚拟机数
                int vn = vmNum / node_number;
                int vn_end = vmNum % node_number + vn;
                int cnt = 0;
                for (int i = 0; i < nodes.size(); i++) {
                    NodeInfo node = nodes.get(i);
                    if (i == nodes.size() - 1) {
                        vn = vn_end;
                    }
                    String Pmip = node.getNodeIp();
                    String url = "http://" + Pmip + sufixUrl;
                    String vmIp = "";
                    //下发镜像文件
               *//* System.out.println("节点" + node.getNodeName() + "下发镜像文件");
                bw.write("节点" + node.getNodeName() + "下发镜像文件\n");
                try {
                    ans = processUtils.dispenseImgByIP("39.98.124.97", docker_image_name, Pmip);
                    System.out.println(ans);
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                }
                if (ans.contains("200")) {
                    if (ans.contains("\"exitStatus\":-1") && !ans.contains("Command execution timed")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("节点" + node.getNodeName() + "下发镜像文件失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false, "", "节点" + node.getNodeName() + "下发镜像文件失败");
                }*//*
                    for (int j = 0; j < vn; j++) {
                        String vmi_name = vm_name + "_" + (cnt + 1);
                        System.out.println("节点" + node.getNodeName() + "创建第" + (cnt + 1) + "个虚拟机");
                        bw.write("节点" + node.getNodeName() + "创建第" + (cnt + 1) + "个虚拟机" + vmi_name + "\n");
                        //创建虚拟机
                        try {
                            ans = processUtils.createVM(vm_image_name, vmi_name, memory[cnt], cpu[cnt], OStype, nettype, Pmip);
                            if (ans.contains("200")) {
                                if (ans.contains("重复")) {
                                    task.setSTATUS(5);
                                    taskUtils.setTask_status(5);
                                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                    bw.write("虚拟机名字重复，创建失败\n");
                                    taskService.updateById(task);
                                    bw.close();
                                    return new CommonResp(false, "", "虚拟机名字重复，创建失败");
                                } else {
                                    QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                                    qw1.eq("name", vmi_name);
                                    VMInfo2 vm = vmService.getOne(qw1);
                                    if (vm.getIp() == null) {
                                        task.setSTATUS(5);
                                        taskUtils.setTask_status(5);
                                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                        bw.write("虚拟机未获得IP，创建失败\n");
                                        taskService.updateById(task);
                                        bw.close();
                                        return new CommonResp(false, "", "虚拟机未获得IP，创建失败\n");

                                    } else {
                                        vmIp = vmService.getOne(qw1).getIp();
                                        System.out.println("第" + (cnt + 1) + "个虚拟机获得IP:" + vmService.getOne(qw1).getIp());
                                        bw.write("第" + (cnt + 1) + "个虚拟机获得IP:" + vmService.getOne(qw1).getIp() + "\n");
                                        if (ips.equals("")) {
                                            ips = vmService.getOne(qw1).getIp();
                                        } else {
                                            ips += "," + vmService.getOne(qw1).getIp();
                                        }
                                    }
                                }
                            } else {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("虚拟机创建失败\n");
                                taskService.updateById(task);
                                bw.close();
                                return new CommonResp(false, "", "虚拟机创建失败");
                            }
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("虚拟机创建失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "虚拟机创建失败");
                        }
                        QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                        qw1.eq("name", vmi_name);
                        VMInfo2 vm = vmService.getOne(qw1);
                    *//*bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹");
                    //执行命令创建文件夹
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    // 构建请求体
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("host", vm.getIp());
                    requestBody.put("username", vm.getUsername());
                    requestBody.put("password", vm.getPasswd());
                    List<String> commands = Arrays.asList(
                            mkcommand
                    );
                    requestBody.put("commands", commands);
                    System.out.println(commands);
                    // 发起请求
                    ResponseEntity<String> response = new RestTemplate().exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody, headers),
                            String.class
                    );
                    if (!response.toString().contains("\"exitStatus\":0")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹失败\n");
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt+1) + "个虚拟机执行命令创建文件夹失败");
                    }*//*
                        //上传文件到虚拟机
                        bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机\n");
                        System.out.println("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机");
                        try {
                            ans = processUtils.uploadDockerToVM(docker_image_name, vmi_name, taskUtils.getPm_ip(),true);
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败\n");
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败");
                        }
                        System.out.println(ans);
                        if (ans.contains("200")) {
                            if (ans.contains("-1") && !ans.contains("Warning")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败\n");
                                taskService.updateById(task);
                                bw.close();
                                return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败");
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败");
                        }
                        bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像\n");
                        System.out.println("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像");
                        //导入镜像
                        try {
                            ans = processUtils.importImage(docker_image_name, vmi_name, Pmip);
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                        }
                        System.out.println(ans);
                        if (ans.contains("200")) {
                            if (ans.contains("-1") && !ans.contains("Command execution timed")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                                taskService.updateById(task);
                                bw.close();
                                return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                        }
                        List<String> c2 = new ArrayList<>();
                        List<String> c1 = new ArrayList<>();
                        for (String s : com) {
                            if (s.contains("sshpass")) {
                                s = "sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/Flower_XW root@"+vmIp+":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/path root@"+vmIp+":/home/pro/";
                                c2.add(s);
                            } else {
                                c1.add(s);
                            }
                        }
                        if (c2.size()>0) {
                            HttpHeaders headers2 = new HttpHeaders();
                            headers2.setContentType(MediaType.APPLICATION_JSON);
                            // 构建请求体
                            Map<String, Object> requestBody2 = new HashMap<>();
                            requestBody2.put("host", node.getNodeIp());
                            requestBody2.put("username", node.getNodeUserName());
                            requestBody2.put("password", node.getNodeUserPasswd());
                            bw.write("端节点" + "执行命令:"+c2+"\n");
                            System.out.println("端节点" + "执行命令:"+c2);
                            requestBody2.put("commands", c2);
                            // 发起请求
                            ResponseEntity<String> response2 = new RestTemplate().exchange(
                                    url,
                                    HttpMethod.POST,
                                    new HttpEntity<>(requestBody2, headers2),
                                    String.class
                            );
                            if (!response2.toString().contains("\"exitStatus\":0")) {
                                task.setSTATUS(5);
                                bw.write("节点" + node.getNodeName() + "执行命令失败\n");
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                bw.close();
                                return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机执行命令失败\n");
                            }
                        }
                        //执行命令运行镜像
                        HttpHeaders headers1 = new HttpHeaders();
                        headers1.setContentType(MediaType.APPLICATION_JSON);
                        // 构建请求体
                        Map<String, Object> requestBody1 = new HashMap<>();
                        requestBody1.put("host", vm.getIp());
                        requestBody1.put("username", vm.getUsername());
                        requestBody1.put("password", vm.getPasswd());
                        requestBody1.put("commands", c1);
                        System.out.println(c1);
                        // 发起请求
                        ResponseEntity<String> response1 = new RestTemplate().exchange(
                                url,
                                HttpMethod.POST,
                                new HttpEntity<>(requestBody1, headers1),
                                String.class
                        );
                        if (!response1.toString().contains("\"exitStatus\":0")) {
                            task.setSTATUS(5);
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机执行命令运行镜像失败\n");
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "", "节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机执行命令运行镜像失败\n");
                        }
                        cnt++;
                    }
                }

            } else {
                return new CommonResp(false, "", "任务不存在");
            }
            task.setSTATUS(4);
            bw.write("任务" + task.getTASK_NAME() + "成功结束\n");
            bw.write("=====================================\n");
            bw.close();
            taskUtils.setVm_ip(ips);
            taskUtils.setTask_status(4);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            return new CommonResp(true, "", "任务执行成功");
        }else if (taskList.contains(task.getTYPE_ID())){
          if(task.getTYPE_ID() == 35){
              TaskUtils35 taskUtils = null;
              try {
                  taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils35.class);
              } catch (IOException e) {
                  task.setSTATUS(5);
                  taskUtils.setTask_status(5);
                  task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                  taskService.updateById(task);
                  return new CommonResp(false, "", "任务执行失败");
              }
              String[] vmIps = taskUtils.getVm_ip().split(",");
              String cpu_nums = taskUtils.getCpu_num();
              String memorys = taskUtils.getMemory();
              int vmNum = vmIps.length;
              Integer[] cpu = new Integer[vmNum];
              Integer[] memory = new Integer[vmNum];
              String[] cpu_split = cpu_nums.split(",");
              String[] memory_split = memorys.split(",");
//            处理cpu
              if (vmNum == 1) {
                  if (cpu_split.length != 1) {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      taskService.updateById(task);
                      bw.write("CPU数输入有误\n");
                      bw.close();
                      return new CommonResp(false, "", "CPU数输入有误");
                  }
                  cpu[0] = (int) Double.parseDouble(cpu_split[0]);
              } else {
                  if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      taskService.updateById(task);
                      bw.write("CPU数输入有误");
                      bw.close();
                      return new CommonResp(false, "", "CPU数输入有误");
                  } else if (cpu_split.length == 1) {
                      for (int i = 0; i < vmNum; i++) {
                          cpu[i] = (int) Double.parseDouble(cpu_split[0]);
                      }
                  } else if (cpu_split.length == vmNum) {
                      for (int i = 0; i < vmNum; i++) {
                          cpu[i] = (int) (Double.parseDouble(cpu_split[i]));
                      }
                  }
              }
              //处理memory
              if (vmNum == 1) {
                  if (memory_split.length != 1) {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      taskService.updateById(task);
                      bw.write("内存数输入有误\n");
                      bw.close();
                      return new CommonResp(false, "", "内存数输入有误\n");
                  }
                  memory[0] = (int) Double.parseDouble(memory_split[0]);
              } else {
                  if (memory_split.length != 1 && memory_split.length != vmNum) {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      taskService.updateById(task);
                      bw.write("内存数输入有误");
                      bw.close();
                      return new CommonResp(false, "", "内存数输入有误");
                  } else if (memory_split.length == 1) {
                      for (int i = 0; i < vmNum; i++) {
                          memory[i] = (int) Double.parseDouble(memory_split[0]);
                      }
                  } else if (memory_split.length == vmNum) {
                      for (int i = 0; i < vmNum; i++) {
                          memory[i] = (int) Double.parseDouble(memory_split[i]);
                      }
                  }
              }
              for(int i = 0; i < vmIps.length; i++){
                  String ip = vmIps[i];
                  VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));

                  try {
                      ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                  } catch (Exception e) {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      taskService.updateById(task);
                      bw.close();
                      return new CommonResp(false,"","任务执行失败");
                  }
                  if (ans.contains("200")) {
                      if (ans.contains("-1")) {
                          task.setSTATUS(5);
                          taskUtils.setTask_status(5);
                          task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                          bw.write("任务失败\n");
                          taskService.updateById(task);
                          bw.close();
                          return new CommonResp(false, "","任务失败");
                      }
                  } else {
                      task.setSTATUS(5);
                      taskUtils.setTask_status(5);
                      task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                      bw.write("任务失败\n");
                      taskService.updateById(task);
                      bw.close();
                      return new CommonResp(false, "", "任务失败");
                  }
              }
          }
         if(task.getTYPE_ID() == 33 || task.getTYPE_ID() == 44){
            TaskUtils3344 taskUtils = null;
            try {
                taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils3344.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                return new CommonResp(false, "", "任务执行失败");
            }
             String[] vmIps = taskUtils.getVm_ip().split(",");
             String cpu_nums = taskUtils.getCpu_num();
             String memorys = taskUtils.getMemory();
             int vmNum = vmIps.length;
             Integer[] cpu = new Integer[vmNum];
             Integer[] memory = new Integer[vmNum];
             String[] cpu_split = cpu_nums.split(",");
             String[] memory_split = memorys.split(",");
//            处理cpu
             if (vmNum == 1) {
                 if (cpu_split.length != 1) {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     taskService.updateById(task);
                     bw.write("CPU数输入有误\n");
                     bw.close();
                     return new CommonResp(false, "", "CPU数输入有误");
                 }
                 cpu[0] = (int) Double.parseDouble(cpu_split[0]);
             } else {
                 if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     taskService.updateById(task);
                     bw.write("CPU数输入有误");
                     bw.close();
                     return new CommonResp(false, "", "CPU数输入有误");
                 } else if (cpu_split.length == 1) {
                     for (int i = 0; i < vmNum; i++) {
                         cpu[i] = (int) Double.parseDouble(cpu_split[0]);
                     }
                 } else if (cpu_split.length == vmNum) {
                     for (int i = 0; i < vmNum; i++) {
                         cpu[i] = (int) (Double.parseDouble(cpu_split[i]));
                     }
                 }
             }
             //处理memory
             if (vmNum == 1) {
                 if (memory_split.length != 1) {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     taskService.updateById(task);
                     bw.write("内存数输入有误\n");
                     bw.close();
                     return new CommonResp(false, "", "内存数输入有误\n");
                 }
                 memory[0] = (int) Double.parseDouble(memory_split[0]);
             } else {
                 if (memory_split.length != 1 && memory_split.length != vmNum) {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     taskService.updateById(task);
                     bw.write("内存数输入有误");
                     bw.close();
                     return new CommonResp(false, "", "内存数输入有误");
                 } else if (memory_split.length == 1) {
                     for (int i = 0; i < vmNum; i++) {
                         memory[i] = (int) Double.parseDouble(memory_split[0]);
                     }
                 } else if (memory_split.length == vmNum) {
                     for (int i = 0; i < vmNum; i++) {
                         memory[i] = (int) Double.parseDouble(memory_split[i]);
                     }
                 }
             }
             for(int i = 0; i < vmIps.length; i++){
                 String ip = vmIps[i];
                 VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));

                 try {
                     ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                 } catch (Exception e) {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     taskService.updateById(task);
                     bw.close();
                     return new CommonResp(false,"","任务执行失败");
                 }
                 if (ans.contains("200")) {
                     if (ans.contains("-1")) {
                         task.setSTATUS(5);
                         taskUtils.setTask_status(5);
                         task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                         bw.write("任务失败\n");
                         taskService.updateById(task);
                         bw.close();
                         return new CommonResp(false, "","任务失败");
                     }
                 } else {
                     task.setSTATUS(5);
                     taskUtils.setTask_status(5);
                     task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                     bw.write("任务失败\n");
                     taskService.updateById(task);
                     bw.close();
                     return new CommonResp(false, "", "任务失败");
                 }
             }
         }

        if(task.getTYPE_ID() == 34 || task.getTYPE_ID() == 42){
            TaskUtils344142 taskUtils = null;
            try {
                taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils344142.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                return new CommonResp(false, "", "任务执行失败");
            }
            String[] vmIps = taskUtils.getVm_ip().split(",");
            String cpu_nums = taskUtils.getCpu_num();
            String memorys = taskUtils.getMemory();
            int vmNum = vmIps.length;
            Integer[] cpu = new Integer[vmNum];
            Integer[] memory = new Integer[vmNum];
            String[] cpu_split = cpu_nums.split(",");
            String[] memory_split = memorys.split(",");
//            处理cpu
            if (vmNum == 1) {
                if (cpu_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("CPU数输入有误\n");
                    bw.close();
                    return new CommonResp(false, "", "CPU数输入有误");
                }
                cpu[0] = (int) Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("CPU数输入有误");
                    bw.close();
                    return new CommonResp(false, "", "CPU数输入有误");
                } else if (cpu_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        cpu[i] = (int) Double.parseDouble(cpu_split[0]);
                    }
                } else if (cpu_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        cpu[i] = (int) (Double.parseDouble(cpu_split[i]));
                    }
                }
            }
            //处理memory
            if (vmNum == 1) {
                if (memory_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("内存数输入有误\n");
                    bw.close();
                    return new CommonResp(false, "", "内存数输入有误\n");
                }
                memory[0] = (int) Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("内存数输入有误");
                    bw.close();
                    return new CommonResp(false, "", "内存数输入有误");
                } else if (memory_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        memory[i] = (int) Double.parseDouble(memory_split[0]);
                    }
                } else if (memory_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        memory[i] = (int) Double.parseDouble(memory_split[i]);
                    }
                }
            }
            for(int i = 0; i < vmIps.length; i++){
                String ip = vmIps[i];
                VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));

                try {
                    ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false,"","任务执行失败");
                }
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("任务失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "","任务失败");
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("任务失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return new CommonResp(false, "", "任务失败");
                }
            }
        }
            if(task.getTYPE_ID() == 39 || task.getTYPE_ID() == 43|| task.getTYPE_ID() == 45){
                TaskUtils394345 taskUtils = null;
                try {
                    taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils394345.class);
                } catch (IOException e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return new CommonResp(false, "", "任务执行失败");
                }
                String[] vmIps = taskUtils.getVm_ip().split(",");
                String cpu_nums = taskUtils.getCpu_num();
                String memorys = taskUtils.getMemory();
                int vmNum = vmIps.length;
                Integer[] cpu = new Integer[vmNum];
                Integer[] memory = new Integer[vmNum];
                String[] cpu_split = cpu_nums.split(",");
                String[] memory_split = memorys.split(",");
//            处理cpu
                if (vmNum == 1) {
                    if (cpu_split.length != 1) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("CPU数输入有误\n");
                        bw.close();
                        return new CommonResp(false, "", "CPU数输入有误");
                    }
                    cpu[0] = (int) Double.parseDouble(cpu_split[0]);
                } else {
                    if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("CPU数输入有误");
                        bw.close();
                        return new CommonResp(false, "", "CPU数输入有误");
                    } else if (cpu_split.length == 1) {
                        for (int i = 0; i < vmNum; i++) {
                            cpu[i] = (int) Double.parseDouble(cpu_split[0]);
                        }
                    } else if (cpu_split.length == vmNum) {
                        for (int i = 0; i < vmNum; i++) {
                            cpu[i] = (int) (Double.parseDouble(cpu_split[i]));
                        }
                    }
                }
                //处理memory
                if (vmNum == 1) {
                    if (memory_split.length != 1) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("内存数输入有误\n");
                        bw.close();
                        return new CommonResp(false, "", "内存数输入有误\n");
                    }
                    memory[0] = (int) Double.parseDouble(memory_split[0]);
                } else {
                    if (memory_split.length != 1 && memory_split.length != vmNum) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.write("内存数输入有误");
                        bw.close();
                        return new CommonResp(false, "", "内存数输入有误");
                    } else if (memory_split.length == 1) {
                        for (int i = 0; i < vmNum; i++) {
                            memory[i] = (int) Double.parseDouble(memory_split[0]);
                        }
                    } else if (memory_split.length == vmNum) {
                        for (int i = 0; i < vmNum; i++) {
                            memory[i] = (int) Double.parseDouble(memory_split[i]);
                        }
                    }
                }
                for(int i = 0; i < vmIps.length; i++){
                    String ip = vmIps[i];
                    VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));

                    try {
                        ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                    } catch (Exception e) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false,"","任务执行失败");
                    }
                    if (ans.contains("200")) {
                        if (ans.contains("-1")) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("任务失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return new CommonResp(false, "","任务失败");
                        }
                    } else {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("任务失败\n");
                        taskService.updateById(task);
                        bw.close();
                        return new CommonResp(false, "", "任务失败");
                    }
                }
            }
        }else {
            return new CommonResp(false,"","任务类型不存在");
        }
        return new CommonResp(true,"","任务执行成功");
    }*/



    /**
     * 一步执行任务
     *
     * @param taskName 任务名
     * @return
     * @throws Exception
     */
  /*  @RequestMapping(value = "/start", method = RequestMethod.POST)
//    @ApiOperation(value = "执行任务", notes = "根据任务名查找数据库按照任务顺序执行子任务")
    @ResponseBody
    public CommonResp startTask(@RequestParam String taskName) throws Exception {
        ProcessUtils processUtils = new ProcessUtils();
        QueryWrapper<ConstructionInfo> qw = new QueryWrapper<>();
        qw.eq("TaskName", taskName);
        qw.orderByAsc("OperationOrder");
        List<ConstructionInfo> tasks = constructionService.list(qw);
        for (ConstructionInfo task :
                tasks) {
            System.out.println("=================");
            System.out.println("步骤" + task.getOperationOrder());
            if (task.getTaskType().contains("CreateVM")) {
                String ans = processUtils.createVM(task.getImgName(), task.getVmName(), task.getMemory(), task.getCPUNum(), task.getOSType(), task.getNetType(), task.getServerIp());
                System.out.println(ans);
                if (ans.contains("200")) {
                    if (ans.contains("重复")) {
                        return new CommonResp(false,"","创建失败，虚拟机名重复");
                    } else {
                        task.setOperationStatus(1);
                        constructionService.updateById(task);
                    }
                } else {
                    return new CommonResp(false,"","创建失败");
                }
            } else if (task.getTaskType().contains("Dispense")) {
                String ans = processUtils.dispenseImgByIP("39.98.124.97", task.getFileName(), task.getServerIp());
                System.out.println(ans);
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        return new CommonResp(false,"","文件下发失败");
                    } else {
                        task.setOperationStatus(1);
                        constructionService.updateById(task);
                    }
                } else {
                    return new CommonResp(false,"","文件下发失败");
                }
            } else if (task.getTaskType().contains("Upload")) {
                QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                qw1.eq("name", task.getVmName());
                VMInfo2 vm = vmService.getOne(qw1);
                if (vm.getIp() == null) {
                    boolean flag = false;
                    for (int i = 0; i < 8; ++i) {
                        if (vmService.getOne(qw1).getIp() == null || vmService.getOne(qw1).getIp().isEmpty()) {
                            Thread.sleep(6000);
                            libvirtService.getallVMip(task.getServerIp());
                        } else {
                            flag = true;
                            break;
                        }
                    }
                    if (flag == false)
                        return new CommonResp(false,"","虚拟机IP不存在，上传文件失败");
                }
                String ans = processUtils.uploadDockerToVM(task.getFileName(), task.getVmName(), task.getServerIp(),flag);
                System.out.println(ans);
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        return new CommonResp(false,"","上传文件失败");
                    } else {
                        task.setOperationStatus(1);
                        constructionService.updateById(task);
                    }
                } else {
                    return new CommonResp(false,"","上传文件失败");
                }
            } else if (task.getTaskType().contains("Import")) {
                String ans = processUtils.importImage(task.getFileName(), task.getVmName(), task.getServerIp());
                System.out.println(ans);
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        return new CommonResp(false,"","导入镜像失败");
                    } else {
                        task.setOperationStatus(1);
                        constructionService.updateById(task);
                    }
                } else {
                    return new CommonResp(false,"","导入镜像失败");
                }
            } else if (task.getTaskType().contains("ExecuteCommand")) {
                String url = "http://" + task.getServerIp() + sufixUrl;
                String command = task.getCmd();
                QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                qw1.eq("name", task.getVmName());
                VMInfo2 vm = vmService.getOne(qw1);
                if (vm == null || vm.getIp() == null)
                    return  new CommonResp(false,"","虚拟机IP不存在");
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 构建请求体
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("host", vm.getIp());
                requestBody.put("username", vm.getUsername());
                requestBody.put("password", vm.getPasswd());
                List<String> commands = Arrays.asList(
                        command
                );
                requestBody.put("commands", commands);
                System.out.println(commands);
                // 发起请求
                ResponseEntity<String> response = new RestTemplate().exchange(
                        url,
                        HttpMethod.POST,
                        new HttpEntity<>(requestBody, headers),
                        String.class
                );
                if (!response.toString().contains("\"exitStatus\":0")) {
                    return  new CommonResp(false,"","执行命令失败");
                }else {
                    task.setOperationStatus(1);
                    constructionService.updateById(task);
                }
            } else {
                return new CommonResp(false,"","任务类型不存在");
            }
        }
        return new CommonResp(true,"","任务执行成功");
    }

    @RequestMapping(value = "/process", method = RequestMethod.POST)
//    @ApiOperation(value = "执行子任务", notes = "执行任务中的一个子任务")
    @ResponseBody
    public CommonResp processTask(@RequestParam String taskName, @RequestParam String taskType, @RequestParam Integer order) throws Exception {
        ProcessUtils processUtils = new ProcessUtils();
        QueryWrapper<ConstructionInfo> qw = new QueryWrapper<>();
        qw.eq("TaskName", taskName);
        qw.eq("TaskType", taskType);
        qw.eq("OperationOrder", order);
        ConstructionInfo task = constructionService.getOne(qw);
        if (taskType.contains("CreateVM")) {
            String ans = processUtils.createVM(task.getImgName(), task.getVmName(), task.getMemory(), task.getCPUNum(), task.getOSType(), task.getNetType(), task.getServerIp());
            System.out.println(ans);
            if (ans.contains("200")) {
                if (ans.contains("重复")) {
                    return new CommonResp(false,"","创建失败，虚拟机名重复");
                } else {
                    task.setOperationStatus(1);
                    constructionService.updateById(task);
                    return  new CommonResp(true,"","虚拟机创建成功");
                }
            } else {
                return new CommonResp(false,"","创建失败");
            }
        } else if (taskType.contains("Dispense")) {
            String ans = processUtils.dispenseImgByIP("39.98.124.97", task.getFileName(), task.getServerIp());
            System.out.println(ans);
            if (ans.contains("200")) {
                if (ans.contains("-1")) {
                    return new CommonResp(false,"","文件下发失败");
                } else {
                    task.setOperationStatus(1);
                    constructionService.updateById(task);
                    return new CommonResp(true,"","文件下发成功");
                }
            } else {
                return new CommonResp(false,"","文件下发失败");
            }
        } else if (taskType.contains("Upload")) {
            QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
            qw1.eq("name", task.getVmName());
            VMInfo2 vm = vmService.getOne(qw1);
            if (vm.getIp() == null)
                return new CommonResp(false,"","虚拟机IP不存在，上传文件失败");
            String ans = processUtils.uploadDockerToVM(task.getFileName(), task.getVmName(), task.getServerIp());
            System.out.println(ans);
            if (ans.contains("200")) {
                if (ans.contains("-1")) {
                    return new CommonResp(false,"","文件上传失败");
                } else {
                    task.setOperationStatus(1);
                    constructionService.updateById(task);
                    return new CommonResp(true,"","文件上传成功");
                }
            } else {
                return new CommonResp(false,"","文件上传失败");
            }
        } else if (taskType.contains("Import")) {
            String ans = processUtils.importImage(task.getFileName(), task.getVmName(), task.getServerIp());
            System.out.println(ans);
            if (ans.contains("200")) {
                if (ans.contains("-1")) {
                    return new CommonResp(false,"","导入镜像失败");
                } else {
                    task.setOperationStatus(1);
                    constructionService.updateById(task);
                    return new CommonResp(true,"","导入镜像成功");
                }
            } else {
                return new CommonResp(false,"","导入镜像失败");
            }
        } else if (taskType.contains("ExecuteCommand")) {

            String url = "http://" + task.getServerIp() + sufixUrl;
            String command = task.getCmd();
            QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
            qw1.eq("name", task.getVmName());
            VMInfo2 vm = vmService.getOne(qw1);
            if (vm == null || vm.getIp() == null)
                return new CommonResp(false,"","虚拟机IP不存在，执行命令失败");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", vm.getIp());
            requestBody.put("username", vm.getUsername());
            requestBody.put("password", vm.getPasswd());
            List<String> commands = Arrays.asList(
                    command
            );
            requestBody.put("commands", commands);
            System.out.println(commands);
            // 发起请求
            ResponseEntity<String> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            if (!response.toString().contains("\"exitStatus\":0")) {
                return new CommonResp(false,"","执行命令失败");
            }else {
                task.setOperationStatus(1);
                constructionService.updateById(task);
            }
        } else {
            return  new CommonResp(false,"","任务类型不存在");
        }
        return new CommonResp(true,"","执行任务成功");
    }*/
    @GetMapping("/downloadLog")
    @ApiOperation(value = "创建虚拟机日志", notes = "创建虚拟机日志")
    public ResponseEntity<Resource> downloadFile() {
        // 文件存储路径，请根据实际情况修改
        String filePath = "/opt/"+"taskLog.txt";
        // 读取文件
        Path path = Paths.get(filePath);
        Resource resource = null;
        try {
            resource = new org.springframework.core.io.UrlResource(path.toUri());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 检查文件是否存在并可读
        if (resource != null && resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
