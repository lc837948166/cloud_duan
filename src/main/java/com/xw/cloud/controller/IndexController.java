package com.xw.cloud.controller;

import com.google.gson.Gson;
import com.jcraft.jsch.*;
import com.xw.cloud.bean.VmInfo;
import com.xw.cloud.inter.OperationLogDesc;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;
@Api(tags = "Kubernetes 指标管理", description = "获取和管理 Kubernetes 集群中的 Pods 和 Nodes 指标")
@Controller
@CrossOrigin
@RequestMapping("/index")
public class IndexController {

    @ApiOperation(value = "查看 Pod 指标页面", notes = "返回展示 Pod 指标的页面路径")
    @RequestMapping(value = "/podIndex", method = RequestMethod.GET)
    @OperationLogDesc(module = "指标管理", events = "查看 Pod 指标页面")
    public String pv() {
        return "index/podIndex";
    }

    /*
    @RequestMapping(value = "/podInedx/lists", method = RequestMethod.GET)
    public ModelAndView getPvList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        OkHttpClient httpClient = new OkHttpClient();

        String username = "root";
        String password = "@wsad1234";

        String token = "K107655c1159f806cbdd10648a925f6b59084e857e828285da54c0bd058887f21b5::server:79d8ec9c45d738d811703cd0986d14d3";
        String credentials = "Bearer " + token;


        Request request = new Request.Builder()
                .url("https://192.168.174.164:6443/apis/metrics.k8s.io/v1beta1/pods")
                .header("Authorization", credentials)
                .build();

        Response response = httpClient.newCall(request).execute();





        // 解析返回的 JSON 数据
        Gson gson = new Gson();
        String responseBody = response.body().string();
        // 处理返回的指标数据
        System.out.println(responseBody);

        modelAndView.addObject("result", responseBody);

        return modelAndView;
    }
*/

    //    @RequestMapping(value = "/podIndex/list", method = RequestMethod.GET)
    @ApiOperation(value = "获取 Pod 指标列表", notes = "获取 Kubernetes 集群中所有 Pods 的指标")
    @ApiResponses({
            @ApiResponse(code = 200, message = "成功获取 Pod 指标列表"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @RequestMapping(value = "/podIndex/list", method = RequestMethod.POST)
    @OperationLogDesc(module = "指标管理", events = "获取容器指标")
    public ModelAndView getpodIndexList(@RequestBody VmInfo vmInfo) throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
//        String userName = "root";
//        String userPassword = "@wsad1234";
//        String virtualMachineIp = "192.168.174.133";

        String userName = vmInfo.getUserName();
        String userPassword = vmInfo.getUserPassword();
        String virtualMachineIp = vmInfo.getVirtualMachineIp();

        Session session = null;

        StringBuilder result = new StringBuilder();
        try {

            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("kubectl get --raw /apis/metrics.k8s.io/v1beta1/pods"); // 设置执行的命令
            InputStream in = execChannel.getInputStream();  // 获取命令执行结果的输入流

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
                    System.out.println("exit-status: " + execChannel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // 处理异常
                }
            }
            System.out.println("----------------");
            System.out.println(commandOutput);  // 输出命令执行结果
            System.out.println("----------------");

            modelAndView.addObject("result", commandOutput);
        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }


        return modelAndView;
    }

    //    @RequestMapping(value = "/nodeIndex/list", method = RequestMethod.GET)
    @ApiOperation(value = "获取 Node 指标列表", notes = "获取 Kubernetes 集群中所有 Nodes 的指标")
    @ApiResponses({
            @ApiResponse(code = 200, message = "成功获取 Node 指标列表"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @RequestMapping(value = "/nodeIndex/list", method = RequestMethod.POST)
    @OperationLogDesc(module = "指标管理", events = "获取节点指标")
//    public ModelAndView getnodeIndexList() throws IOException, ApiException {
    public ModelAndView getNodeIndexList(@RequestBody VmInfo vmInfo) throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
//        String userName = "root";
//        String userPassword = "@wsad1234";
//        String virtualMachineIp = "192.168.174.133";

        String userName = vmInfo.getUserName();
        String userPassword = vmInfo.getUserPassword();
        String virtualMachineIp = vmInfo.getVirtualMachineIp();

        Session session = null;

        StringBuilder result = new StringBuilder();
        try {

            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("kubectl get --raw /apis/metrics.k8s.io/v1beta1/nodes"); // 设置执行的命令
            InputStream in = execChannel.getInputStream();  // 获取命令执行结果的输入流

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
                    System.out.println("exit-status: " + execChannel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    // 处理异常
                }
            }
            System.out.println("----------------");
            System.out.println(commandOutput);  // 输出命令执行结果
            System.out.println("----------------");

            modelAndView.addObject("result", commandOutput);
        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }


        return modelAndView;
    }




}
