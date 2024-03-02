package com.xw.cloud.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xw.cloud.Utils.*;
import com.xw.cloud.bean.*;
import com.xw.cloud.service.*;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import java.io.*;
import java.util.*;

//@Component
//@EnableScheduling
public class TaskJob {


    @Autowired
    private VmService vmService;
    @Autowired
    private NodeService nodeService;

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
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteVM() throws JsonProcessingException
    {
        ProcessUtils processUtils = new ProcessUtils();
        List<Task> tasks = taskService.list(new QueryWrapper<Task>().eq("TYPE_ID", 23));
        String ans = null;
        for(Task task: tasks){
            if(task.getTYPE_ID() != 23){
                continue;
            }
            String task_attributes_values = task.getTASK_ATTRIBUTES_VALUES();
            ObjectMapper mapper = new ObjectMapper();
            TaskUtils taskUtils = null;
            try {
                taskUtils = mapper.readValue(task_attributes_values, TaskUtils.class);
            } catch (IOException e) {
                continue;
            }
            //有一个任务状态是3就认为是程序停止
            if((taskUtils.getTask_status() != 3 && task.getSTATUS()!=3)){
                continue;
            }
            if(taskUtils.getVm_ip() == null || taskUtils.getVm_ip().equals(""))
                continue;
            String[] ips = taskUtils.getVm_ip().split(",");
            String pm_ip = taskUtils.getPm_ip();
            if(ips.length == 0 || pm_ip == null || pm_ip.equals("")){
                continue;
            }
            String anIps = "";//记录删除成功的IP
            System.out.println("task_attr:"+task_attributes_values);
            taskUtils.setTask_status(6);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            for(String ip: ips){
                VMInfo2 vm = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));
                if(vm == null || vm.getName() == null || vm.getName().equals("")){
                    continue;
                }
                try {
                    ans = processUtils.deleteVM(vm.getName(), pm_ip);
                }catch(Exception e) {
                    continue;
                }
                System.out.println("ans:"+ans);
                if(ans.contains("200") && ans.contains("删除成功")){
                    anIps += ip;
                    System.out.println("删除虚拟机成功："+vm.getName());
                }
            }
            String setIp = ",";
            for(int i = 0; i < ips.length;i++){
                if(!anIps.contains(ips[i])){
                    setIp += ","+ips[i];
                }
            }
            taskUtils.setVm_ip(setIp.substring(1));
            if(!taskUtils.getVm_ip().equals("")){
                taskUtils.setTask_status(5);
            }else {
                taskUtils.setTask_status(3);
            }
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
        }
    }
    @Scheduled(cron = "0 */1 * * * ?")
    public void changeVM() throws JsonProcessingException {
        ProcessUtils processUtils = new ProcessUtils();
        ObjectMapper mapper = new ObjectMapper();
        QueryWrapper<Task> qw = new QueryWrapper<>();
        TaskUtils taskUtil23 = null;
        for(Integer t : taskList){
            qw.or().eq("TYPE_ID",t);
        }
        List<Task> tasks = taskService.list(qw);
        String ans;
        Task task;
        List<Task> tasks2 = new ArrayList<>();
        for(Task taskf : tasks){
            String task_attr = taskf.getTASK_ATTRIBUTES_VALUES();
            //有正在执行的任务
            if(task_attr.contains("\"task_status\":6")){
                return;
            }
            //任务类型 任务状态  执行方
            if(taskf.getSTATUS() == 2 && task_attr.contains("\"task_type\":2")&&task_attr.contains("\"task_status\":2")&&(task_attr.contains("\"task_executor\":1")||task_attr.contains("\"task_executor\":3"))){
                tasks2.add(taskf);
            }
        }
        if(tasks2.size()>0){
            task = tasks2.get(0);
        }else {
            return;
        }
        //获取创建虚拟机成功的任务
        List<Task> tasks1 = taskService.list(new QueryWrapper<Task>().eq("TYPE_ID", 23).eq("STATUS", 4));
        if(task.getTYPE_ID() == 35){
            TaskUtils35 taskUtils = null;
            try {
                taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils35.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                return;
            }
            System.out.println("开始执行任务" + task.getTASK_NAME());
            //任务正在执行
            taskUtils.setTask_status(6);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
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
                    return;
                }
                cpu[0] = (int) Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
                    return;
                }
                memory[0] = (int) Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
            //修改每一个虚拟机
            for(int i = 0; i < vmIps.length; i++){
                String ip = vmIps[i];
                VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));
                //  查询所有创建虚拟机的并成功执行的任务
                try {
                    ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
                }
                //调用接口成功
                if (ans.contains("200")) {
                    //修改虚拟机失败
                    if (ans.contains("-1")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        return;
                    }
                    //查找任务类型为23的任务 ，看那个虚任务包含这个IP
                    for(Task task1: tasks1){
                        if(!task1.getTASK_ATTRIBUTES_VALUES().contains(ip))
                            continue;
                        try {
                            taskUtil23 = mapper.readValue(task1.getTASK_ATTRIBUTES_VALUES(), TaskUtils.class);
                        } catch (IOException e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            return;
                        }
                        String[] com = taskUtil23.getCmds().split(",");
                        List<String> c1 = new ArrayList<>();
                        for(String s: com){
                            if(s.contains("docker") && s.contains("run")){
                                String[] s1 = s.split(" ");
                                String containerName = "";
                                for(int k = 0; k < s1.length; k++){
                                    //获取name
                                    if(s1[k].contains("name")){
                                        containerName = s1[k+1];
                                        break;
                                    }
                                }
                                String ss = "docker start "+ containerName;
                                c1.add(ss);
                            }
                        }
                        String url = "http://" + vmInfo2.getServerip() + sufixUrl;
                        if (c1.size()>0) {
                            //执行命令运行镜像
                            HttpHeaders headers1 = new HttpHeaders();
                            headers1.setContentType(MediaType.APPLICATION_JSON);
                            // 构建请求体
                            Map<String, Object> requestBody1 = new HashMap<>();
                            requestBody1.put("host", vmInfo2.getIp());
                            requestBody1.put("username", vmInfo2.getUsername());
                            requestBody1.put("password", vmInfo2.getPasswd());
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
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                return;
                            }else {
                                //找到这个IP，并执行成功 ，不在查找后面的IP
                                break;
                            }
                        }
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
                }
            }
            //每个虚拟机都修改成功 任务才算完成
            task.setSTATUS(4);
            taskUtils.setTask_status(4);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            return ;
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
                return;
            }
            System.out.println("开始执行任务" + task.getTASK_NAME());
            taskUtils.setTask_status(6);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
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
                    return ;
                }
                cpu[0] = (int) Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
                    return ;
                }
                memory[0] = (int) Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return ;
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
            //修改每一个虚拟机
            for(int i = 0; i < vmIps.length; i++){
                String ip = vmIps[i];
                VMInfo2 vmInfo2 = vmService.getOne(new QueryWrapper<VMInfo2>().eq("IP", ip));
                //修改虚拟机信息
                try {
                    ans = processUtils.changeVM(vmInfo2.getServerip(),vmInfo2.getName(),memory[i],cpu[i]);
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return ;
                }
                //调用接口成功
                if (ans.contains("200")) {
                    //信息修改失败
                    if (ans.contains("-1")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        return ;
                    }
                    for(Task task1: tasks1){
                        if(!task1.getTASK_ATTRIBUTES_VALUES().contains(ip))
                            continue;
                        try {
                            taskUtil23 = mapper.readValue(task1.getTASK_ATTRIBUTES_VALUES(), TaskUtils.class);
                        } catch (IOException e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            return;
                        }
                        String[] com = taskUtil23.getCmds().split(",");
                        List<String> c1 = new ArrayList<>();
                        for(String s: com){
                            if(s.contains("docker") && s.contains("run")){
                                String[] s1 = s.split(" ");
                                String containerName = "";
                                for(int k = 0; k < s1.length; k++){
                                    if(s1[k].contains("name")){
                                        containerName = s1[k+1];
                                        break;
                                    }
                                }
                                String ss = "docker start "+ containerName;
                                c1.add(ss);
                            }
                        }
                        String url = "http://" + vmInfo2.getServerip() + sufixUrl;
                        if (c1.size()>0) {
                            //执行命令运行镜像
                            HttpHeaders headers1 = new HttpHeaders();
                            headers1.setContentType(MediaType.APPLICATION_JSON);
                            // 构建请求体
                            Map<String, Object> requestBody1 = new HashMap<>();
                            requestBody1.put("host", vmInfo2.getIp());
                            requestBody1.put("username", vmInfo2.getUsername());
                            requestBody1.put("password", vmInfo2.getPasswd());
                            requestBody1.put("commands", c1);
                            System.out.println(c1);
                            // 发起请求
                            ResponseEntity<String> response1 = new RestTemplate().exchange(
                                    url,
                                    HttpMethod.POST,
                                    new HttpEntity<>(requestBody1, headers1),
                                    String.class
                            );
                            System.out.println("====================");
                            System.out.println(response1.toString());
                            System.out.println("=======================");
                            if (!response1.toString().contains("\"exitStatus\":0")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                return;
                            }else {
                                break;
                            }
                        }
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
                }
            }
            task.setSTATUS(4);
            taskUtils.setTask_status(4);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            return;
        }
        if(task.getTYPE_ID() == 34 || task.getTYPE_ID() == 42 || task.getTYPE_ID() == 41){
            TaskUtils344142 taskUtils = null;
            try {
                taskUtils = mapper.readValue(task.getTASK_ATTRIBUTES_VALUES(), TaskUtils344142.class);
            } catch (IOException e) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                return;
            }
            System.out.println("开始执行任务" + task.getTASK_NAME());
            taskUtils.setTask_status(6);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
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
                    return;
                }
                cpu[0] = (int) Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
                    return;
                }
                memory[0] = (int) Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return ;
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
                    return ;
                }
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        return;
                    }
                    for(Task task1: tasks1){
                        if(!task1.getTASK_ATTRIBUTES_VALUES().contains(ip))
                            continue;
                        try {
                            taskUtil23 = mapper.readValue(task1.getTASK_ATTRIBUTES_VALUES(), TaskUtils.class);
                        } catch (IOException e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            return;
                        }
                        String[] com = taskUtil23.getCmds().split(",");
                        List<String> c1 = new ArrayList<>();
                        for(String s: com){
                            if(s.contains("docker") && s.contains("run")){
                                String[] s1 = s.split(" ");
                                String containerName = "";
                                for(int k = 0; k < s1.length; k++){
                                    if(s1[k].contains("name")){
                                        containerName = s1[k+1];
                                        break;
                                    }
                                }
                                String ss = "docker start "+ containerName;
                                c1.add(ss);
                            }
                        }

                        String url = "http://" + vmInfo2.getServerip() + sufixUrl;
                        if (c1.size()>0) {
                            //执行命令运行镜像
                            HttpHeaders headers1 = new HttpHeaders();
                            headers1.setContentType(MediaType.APPLICATION_JSON);
                            // 构建请求体
                            Map<String, Object> requestBody1 = new HashMap<>();
                            requestBody1.put("host", vmInfo2.getIp());
                            requestBody1.put("username", vmInfo2.getUsername());
                            requestBody1.put("password", vmInfo2.getPasswd());
                            requestBody1.put("commands", c1);
                            System.out.println(c1);
                            // 发起请求
                            ResponseEntity<String> response1 = new RestTemplate().exchange(
                                    url,
                                    HttpMethod.POST,
                                    new HttpEntity<>(requestBody1, headers1),
                                    String.class
                            );
                            System.out.println(response1.toString());
                            if (!response1.toString().contains("\"exitStatus\":0")) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                return;
                            }else {
                                break;
                            }
                        }
                    }

                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
                }
            }
            task.setSTATUS(4);
            taskUtils.setTask_status(4);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            return;
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
                return;
            }
            System.out.println("开始执行任务" + task.getTASK_NAME());
            taskUtils.setTask_status(6);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
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
                    return;
                }
                cpu[0] = (int) Double.parseDouble(cpu_split[0]);
            } else {
                if (cpu_split.length != 1 && cpu_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
                    return;
                }
                memory[0] = (int) Double.parseDouble(memory_split[0]);
            } else {
                if (memory_split.length != 1 && memory_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return;
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
                    return ;
                }
                if (ans.contains("200")) {
                    if (ans.contains("-1")) {
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        return ;
                    }
                    for(Task task1: tasks1){
                        if(!task1.getTASK_ATTRIBUTES_VALUES().contains(ip))
                            continue;
                        try {
                            taskUtil23 = mapper.readValue(task1.getTASK_ATTRIBUTES_VALUES(), TaskUtils.class);
                        } catch (IOException e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            return;
                        }
                        String[] com = taskUtil23.getCmds().split(",");
                        List<String> c1 = new ArrayList<>();
                        for(String s: com){
                            if(s.contains("docker") && s.contains("run")){
                                String[] s1 = s.split(" ");
                                String containerName = "";
                                for(int k = 0; k < s1.length; k++){
                                    if(s1[k].contains("name")){
                                        containerName = s1[k+1];
                                        break;
                                    }
                                }
                                String ss = "docker start "+ containerName;
                                c1.add(ss);
                            }
                        }

                        String url = "http://" + vmInfo2.getServerip() + sufixUrl;
                        if (c1.size()>0) {
                            //执行命令运行镜像
                            HttpHeaders headers1 = new HttpHeaders();
                            headers1.setContentType(MediaType.APPLICATION_JSON);
                            // 构建请求体
                            Map<String, Object> requestBody1 = new HashMap<>();
                            requestBody1.put("host", vmInfo2.getIp());
                            requestBody1.put("username", vmInfo2.getUsername());
                            requestBody1.put("password", vmInfo2.getPasswd());
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
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                taskService.updateById(task);
                                return;
                            }else {
                                break;
                            }
                        }
                    }
                } else {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    return ;
                }
            }
            task.setSTATUS(4);
            taskUtils.setTask_status(4);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
        }
    }
    @Scheduled(cron = "0 */1 * * * ?")
    public void createVM() throws IOException {
        ProcessUtils processUtils = new ProcessUtils();
        String filePath = "/opt/" + "taskLog.txt";
        String OStype = "X86";
        String nettype = "nat";
        String ans = null;
        String ips = "";
        BufferedWriter bw = null;
        Task task;
        ObjectMapper mapper = new ObjectMapper();
        QueryWrapper<Task> queryWrapper = new QueryWrapper<>();
        //创建虚拟机任务  状态为2的任务
        queryWrapper.eq("TYPE_ID", 23);
        queryWrapper.eq("STATUS",2);
        List<Task> tasks = taskService.list(queryWrapper);
        List<Task> tasks2 = new ArrayList<>();
        for(Task taskf : tasks){
            String task_attr = taskf.getTASK_ATTRIBUTES_VALUES();
            //有任务正在执行  直接返回
            if(task_attr.contains("\"task_status\":6")){
                return;
            }
            //如果任务类型 为1  状态为2   执行方为1 或 3 加入任务列表
            if(task_attr.contains("\"task_type\":1")&&task_attr.contains("\"task_status\":2")&&(task_attr.contains("\"task_executor\":1")||task_attr.contains("\"task_executor\":3"))){
                tasks2.add(taskf);
            }
        }
        if(tasks2.size()>0){
            //获取第一个任务
            task = tasks2.get(0);
        }else {
            return ;
        }
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
            return;
        }

        try {
            bw = new BufferedWriter(new FileWriter(filePath,true));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("==============================================");
        System.out.println("开始执行任务" + task.getTASK_NAME());
        bw.write("==============================================\n");
        bw.write("开始执行任务" + task.getTASK_NAME() + "，任务ID为" + task.getTASK_ID() + "\n");
        taskUtils.setTask_status(6);
        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
        taskService.updateById(task);
        int vmNum = taskUtils.getVm_num();
        String vm_image_name = taskUtils.getVm_image_name();
        String vm_name = taskUtils.getVm_name();
        String memorys = taskUtils.getMemory();
        String cpu_nums = taskUtils.getCpu_num();
        String diskss = taskUtils.getDisk();
        String docker_image_name = taskUtils.getDocker_image_name();
        String bandwidths = taskUtils.getBandwidth();
        boolean flag_bw = false;  //是否限制带宽 默认不限制
        String[] com = taskUtils.getCmds().split(",");
        String[] ipip = taskUtils.getPm_ip().split(",");
        String execution_method = taskUtils.getExecution_method();
        String usetype = "other";
        if (vm_image_name == null || vm_name.equals("")) {
            task.setSTATUS(5);
            taskUtils.setTask_status(5);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            bw.write("缺少虚拟机镜像名\n");
            bw.close();
            return;
        }
        if(bandwidths!=null && !bandwidths.equals("")){
            flag_bw = true;
        }
        if(execution_method == null || execution_method.equals("")){
            task.setSTATUS(5);
            taskUtils.setTask_status(5);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            bw.write("缺少执行方法\n");
            bw.close();
            return;
        }
        if(execution_method.contains("fl")
                || execution_method.contains("FL")
                || task.getTASK_NAME().contains("联邦")
                || task.getTASK_NAME().contains("fl")
                || task.getTASK_NAME().contains("FL")
                || task.getTASK_DESCRIPTION().contains("fl")
                || task.getTASK_DESCRIPTION().contains("FL")
                || task.getTASK_DESCRIPTION().contains("联邦")){
            usetype = "federal";
        }else if(execution_method.contains("BC")
                || execution_method.contains("blockchain")
                || task.getTASK_NAME().contains("区块")
                || task.getTASK_NAME().contains("blockchain")
                || task.getTASK_DESCRIPTION().contains("区块")
                || task.getTASK_DESCRIPTION().contains("blockchain")){
            usetype = "blockchain";
        }

        if((task.getTASK_NAME().contains("联邦") && task.getTASK_NAME().contains("区块"))||(task.getTASK_DESCRIPTION().contains("联邦") && task.getTASK_DESCRIPTION().contains("区块"))){
            usetype = "flbc";
        }

        vm_image_name = "centos.qcow2";
//        if (docker_image_name == null || docker_image_name.equals("")) {
//            task.setSTATUS(5);
//            taskUtils.setTask_status(5);
//            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
//            taskService.updateById(task);
//            bw.write("缺少Docker镜像名\n");
//            bw.close();
//            return;
//        }
        if (com == null || com.equals("")) {
            task.setSTATUS(5);
            taskUtils.setTask_status(5);
            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
            taskService.updateById(task);
            bw.write("缺少执行命令\n");
            bw.close();
            return;
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
                    return ;
                }
                nodes.add(node);
            }
        }
        int node_number = nodes.size();
        //处理Disk memory,cpu
        Integer[] cpu = new Integer[vmNum];
        Integer[] memory = new Integer[vmNum];
        Integer[] disk = new Integer[vmNum];
        Integer[] bws = new Integer[vmNum];
        String[] cpu_split = cpu_nums.split(",");
        String[] memory_split = memorys.split(",");
        String[] disk_split = diskss.split(",");
        String[] bws_split = bandwidths.split(",");
//            处理cpu
        if (vmNum == 1) {
            if (cpu_split.length != 1) {
                task.setSTATUS(5);
                taskUtils.setTask_status(5);
                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                taskService.updateById(task);
                bw.write("CPU数输入有误\n");
                bw.close();
                return;
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
                return;
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
                return;
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
                return;
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
                return;
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
                return;
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
        if(flag_bw){
            //处理bw
            if (vmNum == 1) {
                if (bws_split.length != 1) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("带宽输入有误\n");
                    bw.close();
                    return;
                }
                bws[0] = (int) Double.parseDouble(bws_split[0]);
            } else {
                if (bws_split.length != 1 && bws_split.length != vmNum) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.write("磁盘数输入有误");
                    bw.close();
                    return;
                } else if (bws_split.length == 1) {
                    for (int i = 0; i < vmNum; i++) {
                        bws[i] = (int) Double.parseDouble(bws_split[0]);
                    }
                } else if (bws_split.length == vmNum) {
                    for (int i = 0; i < vmNum; i++) {
                        bws[i] = (int) Double.parseDouble(bws_split[i]);
                    }
                }
            }
        }
        //计算每个节点需要分配的虚拟机数
        int vn = vmNum / node_number;
        int vn_end = vmNum % node_number + vn;
        int cnt = 0;
        int bw_singe = -1;
        for (int i = 0; i < nodes.size(); i++) {
            NodeInfo node = nodes.get(i);
            if (i == nodes.size() - 1) {
                vn = vn_end;
            }
            String Pmip = node.getNodeIp();
            String url = "http://" + Pmip + sufixUrl;
            String vmIp = "";
            for (int j = 0; j < vn; j++) {
                VMInfo2 vm;
                bw_singe = -1;
                String vmi_name = vm_name + "_" + (cnt + 1);
                System.out.println("节点" + node.getNodeName() + "创建第" + (cnt + 1) + "个虚拟机");
                bw.write("节点" + node.getNodeName() + "创建第" + (cnt + 1) + "个虚拟机" + vmi_name + "\n");
                //创建虚拟机
                try {
                    if(flag_bw)
                        bw_singe = bws[cnt];
                    ans = processUtils.createVM(vm_image_name, vmi_name, memory[cnt], cpu[cnt], OStype, nettype, Pmip,usetype, bw_singe);
                    if (ans.contains("200")) {
                        if (ans.contains("重复")) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("虚拟机名字重复，创建失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return;
                        } else {
                            QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                            qw1.eq("name", vmi_name);
                            vm = vmService.getOne(qw1);
                            if (vm.getIp() == null) {
                                task.setSTATUS(5);
                                taskUtils.setTask_status(5);
                                task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                                bw.write("虚拟机未获得IP，创建失败\n");
                                System.out.println("虚拟机未获得IP，创建失败");
                                taskService.updateById(task);
                                bw.close();
                                return;
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
                        System.out.println(ans);
                        task.setSTATUS(5);
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        bw.write("虚拟机创建失败\n");
                        System.out.println("ans虚拟机创建失败");
                        taskService.updateById(task);
                        bw.close();
                        return;
                    }
                } catch (Exception e) {
                    task.setSTATUS(5);
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    bw.write("虚拟机创建失败\n");
                    taskService.updateById(task);
                    bw.close();
                    return;
                }
                QueryWrapper<VMInfo2> qw1 = new QueryWrapper();
                qw1.eq("name", vmi_name);
                    //上传文件到虚拟机
                    bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机");
                    String[] docker_images = docker_image_name.split(",");
                    for (String docker_image : docker_images) {
                        boolean flag = true;
                        if(docker_image.contains("tar")){
                            flag = false;
                        }
                        try {
                            ans = processUtils.uploadDockerToVM(docker_image, vmi_name, vm.getServerip(),flag);
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败\n");
                            bw.close();
                            return;
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
                                return;
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机上传文件到虚拟机失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return;
                        }
                    }
                if(docker_image_name!=null && !docker_image_name.equals("") && docker_image_name.contains("tar")) {
                    bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像\n");
                    System.out.println("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像");

                    for (String docker_image : docker_images) {
                        //导入镜像
                        if(!docker_image.contains(".tar")){
                            continue;
                        }
                        try {
                            ans = processUtils.importImage(docker_image, vmi_name, Pmip);
                        } catch (Exception e) {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            taskService.updateById(task);
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                            bw.close();
                            return;
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
                                return;
                            }
                        } else {
                            task.setSTATUS(5);
                            taskUtils.setTask_status(5);
                            task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                            bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机导入镜像失败\n");
                            taskService.updateById(task);
                            bw.close();
                            return;
                        }
                    }
                }
                List<String> c2 = new ArrayList<>(); //传文件命令
                List<String> c1 = new ArrayList<>(); //镜像启动命令
                List<String> c3 = new ArrayList<>();
                for (String s : com) {
                    if (s.contains("sshpass")) {
                        if ("federal".equals(usetype)) {
                            int p = (cnt + 1) % 20;
                            if (p == 0)
                                p = 20;
                            s = "sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/Cancer_Predict root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/News_Class root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/Flower_XW root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/path root@" + vmIp + ":/home/pro/";
                            c2.add(s);
                        }else if("blockchain".equals(usetype)){
                            s = "sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/qukuai/testwork root@"+vmIp+":/root & " +
                                "sshpass -p 111 scp -o StrictHostKeyChecking=no /etc/usr/xwfiles/qukuai/node_json.py root@"+vmIp+":/home/pro/appdata & " +
                                "sshpass -p 111 scp -o StrictHostKeyChecking=no /etc/usr/xwfiles/qukuai/qukuailog.py root@"+vmIp+":/home/pro/appdata";
                            c2.add(s);
                        }else if("flbc".equals(usetype)){
                            int p = (cnt + 1) % 20;
                            if (p == 0)
                                p = 20;
                            s = "sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/Cancer_Predict root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/News_Class root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/m" + p + "/Flower_XW root@" + vmIp + ":/home/pro/appdata/ && sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/path root@" + vmIp + ":/home/pro/";
                            c2.add(s);
                            s = "sshpass -p 111 scp -o StrictHostKeyChecking=no -r /etc/usr/xwfiles/qukuai/testwork root@"+vmIp+":/root & " +
                                "sshpass -p 111 scp -o StrictHostKeyChecking=no /etc/usr/xwfiles/qukuai/node_json.py root@"+vmIp+":/home/pro/appdata & " +
                                "sshpass -p 111 scp -o StrictHostKeyChecking=no /etc/usr/xwfiles/qukuai/qukuailog.py root@"+vmIp+":/home/pro/appdata";
                            c2.add(s);
                        }
                    }else {
                        c1.add(s);
                    }
                }
                if(flag_bw&&bw_singe!=0){ //需要执行带宽命令
                    bw_singe  = bw_singe*1000;
                    String ens = vm.getNic();
                    String won = "wondershaper -a "+ens+" "+"-d "+bw_singe+" "+"-u "+bw_singe+"";
                    c3.add(won);
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
                        return;
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
                System.out.println(response1.toString());
                if (!response1.toString().contains("\"exitStatus\":0")) {
                    task.setSTATUS(5);
                    bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机执行命令失败\n");
                    taskUtils.setTask_status(5);
                    task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                    taskService.updateById(task);
                    bw.close();
                    return;
                }
                if(c3.size()>0) {
                    //执行命令运行镜像
                    HttpHeaders headers3 = new HttpHeaders();
                    headers3.setContentType(MediaType.APPLICATION_JSON);
                    // 构建请求体
                    Map<String, Object> requestBody3 = new HashMap<>();
                    requestBody3.put("host", vm.getIp());
                    requestBody3.put("username", vm.getUsername());
                    requestBody3.put("password", vm.getPasswd());
                    requestBody3.put("commands", c3);
                    System.out.println(c3);
                    // 发起请求
                    ResponseEntity<String> response3 = new RestTemplate().exchange(
                            url,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody3, headers3),
                            String.class
                    );
                    System.out.println(response3.toString());
                    if (!response3.toString().contains("\"exitStatus\":0")) {
                        task.setSTATUS(5);
                        bw.write("节点" + node.getNodeName() + "第" + (cnt + 1) + "个虚拟机执行命令失败\n");
                        taskUtils.setTask_status(5);
                        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
                        taskService.updateById(task);
                        bw.close();
                        return;
                    }
                }
                cnt++;
            }
        }
        task.setSTATUS(4);
        bw.write("任务" + task.getTASK_NAME() + "成功结束\n");
        bw.write("==============================================\n");
        bw.close();
        taskUtils.setVm_ip(ips);
        taskUtils.setTask_status(4);
        task.setTASK_ATTRIBUTES_VALUES(mapper.writeValueAsString(taskUtils));
        taskService.updateById(task);
        }
    }

