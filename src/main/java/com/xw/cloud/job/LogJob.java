package com.xw.cloud.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.OperationLog;
import com.xw.cloud.bean.PodLog;
import com.xw.cloud.bean.VMLog;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.impl.NodeServiceImpl;
import com.xw.cloud.service.impl.OperationLogServiceImpl;
import com.xw.cloud.service.impl.PodLogServiceImpl;
import com.xw.cloud.service.impl.VMLogServiceImpl;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
//@Component
//@EnableScheduling
public class LogJob {

  /*  @Value("${k8s.config}")
    private String k8sConfig;

    @Value("${VM.ip}")
    private String virtualMachineIp;
    @Value("${VM.username}")
    private String username;
    @Value("${VM.password}")
    private String password;*/


    @Autowired
    private OperationLogServiceImpl operationLogService;

    @Autowired
    private PodLogServiceImpl podLogService;

    @Autowired
    private VMLogServiceImpl vmLogService;

    @Autowired
    private NodeServiceImpl nodeService;

    private static  Integer saveDays = 1;
    private static  Integer VMSaveDays = 30;
    @OperationLogDesc(module = "日志管理", events = "操作日志定时删除")
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteLog(){
        Date now = new Date();
        String deleteDate= getDeleteDate(now,saveDays);
        try{
            operationLogService.remove(new QueryWrapper<OperationLog>().lt("AddTime",deleteDate));
        }catch (Exception e){
        }
    }
    @OperationLogDesc(module = "日志管理", events = "容器日志定时删除")
    @Scheduled(cron = "0 */1 * * * ?")
    public void deletePodLog(){  // 一天执行一次删容器日志 删除30天之前的
        //容器日志删除
        Date now = new Date();
        String deleteDate= getDeleteDate(now,saveDays);
        try{
            podLogService.remove(new QueryWrapper<PodLog>().lt("AddTime",deleteDate));
        }catch (Exception e){
        }
    }
    @OperationLogDesc(module = "日志管理", events = "虚拟机日志定时删除")
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteVMLog(){ // 一天执行一次虚拟机日志 删除30天之前的
        Date now = new Date();
        String deleteDate= getDeleteDate(now,VMSaveDays);
        try{
            vmLogService.remove(new QueryWrapper<VMLog>().lt("AddTime",deleteDate));
        }catch (Exception e){
        }
    }
    @OperationLogDesc(module = "日志管理", events = "容器日志定时添加")
    @Scheduled(cron = "0 */5 * * * ?")
    public void addPodLog() throws IOException, ApiException, ParseException {
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        List<PodLog> ans = new LinkedList<>();
        // 获取Pod的日志
        V1NamespaceList v1NamespaceList = api.listNamespace(null, null, null, null, null, null, null, null, null, null);
        List<V1Namespace> items = v1NamespaceList.getItems();
        for (V1Namespace v1 : items) {
            String namesapceName = v1.getMetadata().getName();
            V1PodList v1PodList = api.listNamespacedPod(namesapceName, null, null, null, null, null, null, null, null, null, null);
            for (V1Pod pod : v1PodList.getItems()) {
                String name = pod.getMetadata().getName();
                String NodeName = pod.getSpec().getNodeName();
                String podLogs = null;
                try {
                    //  只查询最近三十天的日志 2592000s
                    podLogs = api.readNamespacedPodLog(name, namesapceName, null, null, null, null, null, null, saveDays*24*60*60, null, true);
                } catch (ApiException ae) {
                }
                if (podLogs != null && !podLogs.equals("")) {
                    String[] split = podLogs.split("\n");
                    for (String s : split) {
                        if (s.startsWith("20")) {
                            String date = s.substring(0, 10);
                            String time = s.substring(11, 19);
                            String t = date + " " + time;
                            QueryWrapper qw = new QueryWrapper<>();
                            qw.eq("PodName", name);
                            qw.eq("Spaces", namesapceName);
                            qw.eq("AddTime", t);
                            List list = podLogService.list(qw);
                            SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date da = dateFormat_.parse(t);
                            if (list.size() <= 0) {
                                QueryWrapper qw1 = new QueryWrapper<>();
                                qw1.eq("PodName", name);
                                qw1.eq("Spaces", namesapceName);
                                qw1.like("PODCONTENT", "[WARNING] No files matching import glob pattern: /etc/coredns/custom/*.override");
                                qw1.gt("ADDTIME",getDeleteDateSec(da,60*10));
                                List list1 = podLogService.list(qw1);
                                if(list1.size()>0){
                                    continue;
                                }
                                PodLog l = new PodLog();
                                l.setPodNames(name);
                                l.setSpaces(namesapceName);
                                l.setAddTime(da);
                                l.setPodContent(s);
                                if(s.length() < 100){
                                    l.setDisplayContent(s+"......");
                                }else {
                                    l.setDisplayContent(s.substring(0, 100) + "......");
                                }
                                l.setNodeName(NodeName);
                                podLogService.save(l);
                            }
                        }
                    }
                }
            }
        }
    }

    @OperationLogDesc(module = "日志管理", events = "虚拟机日志定时添加")
    @Scheduled(cron = "0 */5 * * * ?")
    public void addVMLog() throws IOException, ApiException, ParseException, JSchException {
        Session session = null;
        List<NodeInfo> nodes = nodeService.list();
        for (int k = 0; k < nodes.size(); k++) {
            NodeInfo nodeInfo = nodes.get(k);
            if(nodeInfo.getIsSchedulable() != null || nodeInfo.getIsSchedulable() != 1){
                continue;
            }
            StringBuilder result = new StringBuilder();
            JSch jsch = new JSch();
            session = jsch.getSession(nodeInfo.getNodeUserName(), nodeInfo.getNodeIp(), 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(nodeInfo.getNodeUserPasswd());
            session.connect();
            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("ls /var/log/libvirt/qemu"); // 设置执行的命令
            InputStream in = null;
            in = execChannel.getInputStream();  // 获取命令执行结果的输入流
            execChannel.connect();  // 连接远程执行命令
            byte[] tmp = new byte[1024];
            StringBuilder commandOutput = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    commandOutput.append(new String(tmp, 0, i));
                }
                if (execChannel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // 处理异常
                }
            }
            for(String vmName: commandOutput.toString().split("\n")){
                commandOutput.setLength(0);
                if(!vmName.endsWith(".log"))
                    continue;
                Channel execChannel1 = session.openChannel("exec");
                ((ChannelExec) execChannel1).setCommand("cat /var/log/libvirt/qemu/" + vmName); // 设置执行的命令
                in = execChannel1.getInputStream();  // 获取命令执行结果的输入流
                execChannel1.connect();  // 连接远程执行命令
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        commandOutput.append(new String(tmp, 0, i));
                    }
                    if (execChannel1.isClosed()) {
                        if (in.available() > 0) continue;
                        break;
                    }
                }
                String vname = vmName.substring(0,vmName.indexOf("log")-1);
                String[] split = commandOutput.toString().split("\n");
                String ins = "";   //插入数据
                for (String s : split) {
                    if(s.startsWith("20")){    //插入前一条数据
                        if(!ins.equals("")){  //不为空  20开头 此时插入数据
                            String date = ins.substring(0, 10);
                            String time = ins.substring(11, 19);
                            String t = date + " " + time;
                            if(!t.contains("202")){
                                ins = s;
                                continue;
                            }
                            SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date da = dateFormat_.parse(t);
                            QueryWrapper qw = new QueryWrapper<>();
                            qw.eq("VmName", vname);
                            qw.eq("AddTime", t);
                            //增加判断日志是否为30天以内的
                            //VM Container
                            String deleteDate = getDeleteDate(new Date(), VMSaveDays);
                            Date before30 = dateFormat_.parse(deleteDate);  //30天之前
                            if(da.compareTo(before30) > 0) {
                                List list = vmLogService.list(qw);
                                if (list.size() <= 0) {
                                    VMLog vmLog = new VMLog();
                                    vmLog.setVmName(vname);
                                    vmLog.setAddTime(da);
                                    vmLog.setVmContent(ins);
                                    if(ins.length() < 100){
                                        vmLog.setDisplayContent(ins+"......");
                                    }else {
                                        vmLog.setDisplayContent(ins.substring(0, 100) + "......");
                                    }
                                    vmLog.setNodeIp(nodeInfo.getNodeIp());
                                    vmLogService.save(vmLog);
                                }
                            }
                            ins = s;
                        }else {  //  第一个
                            ins += s;
                        }
                    }else {   //  不是20开头 就加到前一个字符串
                        ins += s;
                    }
                }
                //插入最后一天数据
                if(ins!= null && !ins.equals("")) {
                    String date = ins.substring(0, 10);
                    String time = ins.substring(11, 19);
                    String t = date + " " + time;
                    SimpleDateFormat dateFormat_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date da = dateFormat_.parse(t);
                    String deleteDate = getDeleteDate(new Date(), VMSaveDays);
                    Date before30 = dateFormat_.parse(deleteDate);  //30天之前
                    if (da.compareTo(before30) > 0) {
                        QueryWrapper qw = new QueryWrapper<>();
                        qw.eq("VmName", vname);
                        qw.eq("AddTime", t);
                        List list = vmLogService.list(qw);
                        if (list.size() <= 0) {
                            VMLog vmLog = new VMLog();
                            vmLog.setVmName(vname);
                            vmLog.setAddTime(da);
                            vmLog.setVmContent(ins);
                            if (ins.length() < 100) {
                                vmLog.setDisplayContent(ins + "......");
                            } else {
                                vmLog.setDisplayContent(ins.substring(0, 100) + "......");
                            }
                            vmLog.setNodeIp(nodeInfo.getNodeIp());
                            vmLogService.save(vmLog);
                        }
                    }
                }
            }
        }
    }
    public static String getDeleteDate(Date now,int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(calendar.DATE, -days);
        Date delete=calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString=sdf.format(delete);
        return dateString;
    }
    public static String getDeleteDateSec(Date now,int seconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(calendar.SECOND, -seconds);
        Date delete=calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString=sdf.format(delete);
        return dateString;
    }

}
