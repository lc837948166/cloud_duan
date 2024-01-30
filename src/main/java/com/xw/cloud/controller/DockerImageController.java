package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import com.xw.cloud.Utils.SshClient;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.VMInfo2;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.impl.NodeServiceImpl;
import com.xw.cloud.service.impl.VmServiceImpl;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * 整体逻辑：
 * 1、GetMapping：/imageList和/containerList查看镜像和容器列表
 * 2、PostMapping：/mkdir创建虚拟机目录，/upload向虚拟机传镜像文件，/import将镜像文件导入到docker，/run创建容器运行导入的镜像（run需要import导入后拿到ImageName:Tag），
 * /stopContainer停止容器，/startContainer启动容器
 * 3、DeleteMapping：/deleteContainer停止容器并删除，/deleteImage删除容器后才能删除镜像
 */
@Slf4j
@Controller
@CrossOrigin
@RequestMapping("/docker")
@Api(tags = "Docker 镜像管理", description = "管理 Docker 容器中的镜像")
public class DockerImageController {

    @Autowired
    private VmServiceImpl vmService;

    @Autowired
    private NodeServiceImpl nodeService;


    private final String sufixUrl = ":8081/api/ssh/execute2";

    /**
     *
     * @param vmName 端上虚拟机名
     * @param endIp 端ip
     * @return
     */
    @GetMapping("/imageList")
    @ApiOperation("获取虚拟机 Docker 镜像列表")
    @OperationLogDesc(module = "镜像管理", events = "获取虚拟机 Docker 镜像列表")
    public ResponseEntity<List<String>> listImages(@RequestParam("vmName") String vmName,
                                                   @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();


        String url = "http://" + endIp + sufixUrl;
        String command = "docker image ls";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                command
        );
        requestBody.put("commands", commands);

        Gson gson = new Gson();

        System.out.println(commands);
        System.out.println(vmInfo2);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        System.out.println(response);
        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {

            // 将 JSON 字符串解析为一个 Map 对象
            Map<String, Object> resultMap = gson.fromJson(response.getBody(), Map.class);



            String responseBody;

            // 获取 output 字段的值
            responseBody = ((String) resultMap.get("output")).trim();

            // 处理响应字符串
            // 将输出结果按行分割，并返回镜像名称列表
            List<String> images = Arrays.asList(responseBody.split("\n"));
            return ResponseEntity.ok(images);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /**
     * 虚拟机上创建目标目录
     * @param vmName 端上虚拟机名
     * @param targetPath 目标目录
     * @param endIp 端ip
     * @return
     */
    @PostMapping("/mkdir")
    @ApiOperation("创建虚拟机目标目录")
    @OperationLogDesc(module = "镜像管理", events = "创建虚拟机目标目录")
    public ResponseEntity<String> mkdir(@RequestParam("vmName") String vmName,
                                        @RequestParam("targetPath") String targetPath,
                                        @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();

        String url = "http://" + endIp + sufixUrl;


        //加载镜像
        String mkdirCommand = "mkdir -p " + targetPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                mkdirCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }

    }


    /**
     * 在云上调用接口，在端上写sshpass命令向虚拟机传
     * @param fileName 镜像名
     * @param vmName 虚拟机名
     * @param targetPath 存放镜像的文件目录
     * @param endIp 端ip
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("上传到虚拟机 Docker 镜像")
    public ResponseEntity<String> upload(@RequestParam(value = "fileName") String fileName,
                                         @RequestParam("vmName") String vmName,
                                         @RequestParam("targetPath") String targetPath,
                                         @RequestParam("endIp") String endIp,
                                         @RequestParam("sourceIp") String sourceIp) {
         //省去云到端传镜像的步骤 默认Docker镜像直接保存在 端节点上
        // 发起获取文件路径的请求
        String dispenseUrl = "http://39.101.136.242:8081/api/ssh/dispenseImgByIP?sourceip=" + sourceIp + "&fileName=" + fileName + "&endip=" + endIp;
        ResponseEntity<String> dispenseResponse = new RestTemplate().getForEntity(dispenseUrl, String.class);
        if (dispenseResponse.getStatusCode().is2xxSuccessful()) {
            QueryWrapper<NodeInfo> qw1 = new QueryWrapper<>();
            if (vmName != null && !vmName.equals("")) {
                qw1.eq("nodeIp", endIp);
            }
            NodeInfo nodeInfo = nodeService.getOne(qw1);
            String nodeUserName = nodeInfo.getNodeUserName();
            String nodeUserPassword = nodeInfo.getNodeUserPasswd();
            String nodeHost = nodeInfo.getNodeIp();
            QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
            if (vmName != null && !vmName.equals("")) {
                qw.eq("name", vmName);
            }
            VMInfo2 vmInfo2 = vmService.getOne(qw);
            String userName = vmInfo2.getUsername();
            String userPassword = vmInfo2.getPasswd();
            String host = vmInfo2.getIp();
            String url = "http://" + sourceIp + sufixUrl;  //sourceIp应该是端节点IP
            String imagePath = "/etc/usr/xwfiles/";
            String filePath = imagePath + fileName;
            String transCommand = "sshpass -p " + userPassword + " scp -o ConnectTimeout=3 -o StrictHostKeyChecking=no " + filePath + " " + userName + "@" + host + ":" + targetPath;
            System.out.println(imagePath);
            System.out.println(transCommand);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", nodeHost);
            requestBody.put("username", nodeUserName);
            requestBody.put("password", nodeUserPassword);
            List<String> commands = Arrays.asList(
                    transCommand
            );
            requestBody.put("commands", commands);
            // 发起请求  接口是端节点的executeCommand程序， excute2接口 传入主机信息和需要执行的命令
            ResponseEntity<String> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            // 获取响应
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                return ResponseEntity.ok(responseBody);
            } else {
                // 处理请求失败情况
                return ResponseEntity.ok("fail");
            }
        }else{
            return ResponseEntity.ok("fail");
        }
    }


/*    *
     * 在云上调用接口，在端上写sshpass命令向虚拟机传
     * @param fileName 镜像名
     * @param vmName 虚拟机名
     * @param targetPath 存放镜像的文件目录
     * @param endIp 端ip
     * @param sourceIp 云ip
     * @param flag 是否为程序包
     * @return*/
    @PostMapping("/upload1")
    @ApiOperation("上传到虚拟机 Docker 镜像")
    @OperationLogDesc(module = "镜像管理", events = "上传到虚拟机 Docker 镜像")
    public ResponseEntity<String> upload(@RequestParam(value = "fileName") String fileName,
                                         @RequestParam("vmName") String vmName,
                                         @RequestParam("targetPath") String targetPath,
                                         @RequestParam("endIp") String endIp,@RequestParam("flag") boolean flag) {
         //省去云到端传镜像的步骤 默认Docker镜像直接保存在 端节点上
        // 发起获取文件路径的请求
 /*       String dispenseUrl = "http://39.98.124.97:8081/api/ssh/dispenseImgByIP?sourceip=" + sourceIp + "&fileName=" + fileName + "&endip=" + endIp;
        ResponseEntity<String> dispenseResponse = new RestTemplate().getForEntity(dispenseUrl, String.class);
        if (dispenseResponse.getStatusCode().is2xxSuccessful()) {*/
            QueryWrapper<NodeInfo> qw1 = new QueryWrapper<>();
            if (vmName != null && !vmName.equals("")) {
                qw1.eq("nodeIp", endIp);
            }
            NodeInfo nodeInfo = nodeService.getOne(qw1);
            String nodeUserName = nodeInfo.getNodeUserName();
            String nodeUserPassword = nodeInfo.getNodeUserPasswd();
            String nodeHost = nodeInfo.getNodeIp();
            QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
            if (vmName != null && !vmName.equals("")) {
                qw.eq("name", vmName);
            }
            VMInfo2 vmInfo2 = vmService.getOne(qw);
            String userName = vmInfo2.getUsername();
            String userPassword = vmInfo2.getPasswd();
            String host = vmInfo2.getIp();
            String url = "http://" + endIp + sufixUrl;  //sourceIp应该是端节点IP
            String imagePath = "/etc/usr/xwfiles/";
            String filePath = imagePath + fileName;
            String transCommand;
            if(flag)
              transCommand = "sshpass -p " + userPassword + " scp -o StrictHostKeyChecking=no -r " + filePath + " " + userName + "@" + host + ":" + targetPath;
            else
                transCommand = "sshpass -p " + userPassword + " scp -o StrictHostKeyChecking=no " + filePath + " " + userName + "@" + host + ":" + targetPath;
            System.out.println(imagePath);
            System.out.println(transCommand);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", nodeHost);
            requestBody.put("username", nodeUserName);
            requestBody.put("password", nodeUserPassword);
            List<String> commands = Arrays.asList(
                    transCommand
            );
            requestBody.put("commands", commands);
            // 发起请求  接口是端节点的executeCommand程序， excute2接口 传入主机信息和需要执行的命令
            ResponseEntity<String> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            // 获取响应
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                return ResponseEntity.ok(responseBody);
            } else {
                // 处理请求失败情况
                return ResponseEntity.ok("fail");
            }
    /*    }else{
            return ResponseEntity.ok("fail");
        }*/
    }

    /**
     *
     * @param imageFileName 镜像文件名（例如mysql57.tar）
     * @param vmName 虚拟机名
     * @param targetPath 端上虚拟机中存放镜像路径
     * @param endIp 端ip
     * @return
     */
    @PostMapping("/import")
    @ApiOperation("导入虚拟机中的 Docker 镜像")
    @OperationLogDesc(module = "镜像管理", events = "导入虚拟机中的 Docker 镜像")
    public ResponseEntity<String> importImage(@RequestParam("imageFileName") String imageFileName,
                                              @RequestParam("vmName") String vmName,
                                              @RequestParam("targetPath") String targetPath,
                                              @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();

        String url = "http://" + endIp + sufixUrl;
        String filePath = targetPath + imageFileName;

        System.out.println(filePath);

        //加载镜像
        String uploadCommand = "docker load -i " + filePath;
        System.out.println(uploadCommand);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                uploadCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );
        System.out.println(response);

        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }
    }


    /**
     *run了之后才能创建容器
     * @param command 镜像名（通过镜像列表查看到到的，docker run -d mysql:5.7,  镜像名：标签）
     * @param vmName 端上虚拟机名
     * @param endIp 端节点ip
     * @return
     */
    @PostMapping("/run")
    @ApiOperation("运行虚拟机中的 Docker 容器")
    @OperationLogDesc(module = "镜像管理", events = "运行虚拟机中的 Docker 容器")
    public ResponseEntity<String> runContainer(@RequestParam("command") String command,
                                               @RequestParam("vmName") String vmName,
                                               @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();


        String url = "http://" + endIp + sufixUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                command
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }
    }

    //自己测试用
    @GetMapping("/listContainer")
    @ApiOperation("Docker 容器")
    public ResponseEntity<String> listContainer(
                                                @RequestParam("vmName") String vmName) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();

        System.out.println(userName+userPassword+host);

        String command = "docker ps -a ";
        String command1 = "docker image ls ";

        SshClient sshClient = new SshClient();
        String output = sshClient.runCommand(host, userName, userPassword, command);
        List<String> images = Arrays.asList(output.split("\n"));

        for (String list : images) {
            System.out.println(list);

        }
        return ResponseEntity.ok(output);
    }

    /**
     * 删除镜像需要先删除容器
     * @param imageName 镜像名（通过镜像列表查看到到的，例如mysql:5.7,  镜像名：标签）
     * @param vmName 端上虚拟机名
     * @param endIp 端ip
     * @return
     */
    @DeleteMapping("/deleteImage")
    @ApiOperation("删除虚拟机中 Docker 镜像")
    @OperationLogDesc(module = "镜像管理", events = "删除虚拟机中 Docker 镜像")
    public ResponseEntity<String> deleteImage(@RequestParam("imageName") String imageName,
                                              @RequestParam("vmName") String vmName,
                                              @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String output = String.valueOf(searchContainerByImage(imageName, vmName, endIp).getBody());
        String deleteImageCommand = "docker rmi " + imageName;
        String url = "http://" + endIp + sufixUrl;
        String containerId = "";
        Gson gson = new Gson();

        try {
            // 将 JSON 字符串解析为一个 Map 对象
            Map<String, Object> resultMap = gson.fromJson(output, Map.class);
            // 获取 output 字段的值
            containerId = ((String) resultMap.get("output")).trim();
            System.out.println(containerId);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 先删除容器
        ResponseEntity<String> deleteContainerResponse = deleteContainer(containerId, vmName, endIp);

        System.out.println(deleteContainerResponse);
        if (deleteContainerResponse.getStatusCode().is2xxSuccessful()) {
            // 删除容器成功，再使用 Docker 命令行工具删除镜像
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", host);
            requestBody.put("username", userName);
            requestBody.put("password", userPassword);
            List<String> commands = Arrays.asList(
                    deleteImageCommand
            );
            requestBody.put("commands", commands);

            // 发起请求
            ResponseEntity<String> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            // 获取响应
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                return ResponseEntity.ok(responseBody);
            } else {
                // 处理请求失败情况
                return ResponseEntity.ok("fail");
            }
        } else {
            // 删除容器失败，直接返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除容器失败");
        }
    }

    @GetMapping("/searchContainerByImage")
    @ApiOperation("查询虚拟机中 Docker 容器")
    @OperationLogDesc(module = "镜像管理", events = "查询虚拟机中 Docker 容器")
    public ResponseEntity<String> searchContainerByImage(@RequestParam("imageName") String imageName,
                                              @RequestParam("vmName") String vmName,
                                              @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String searchCommand = "docker ps -aqf ancestor=" + imageName;
        String url = "http://" + endIp + sufixUrl;


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                searchCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }

    }

    /**
     *
     * @param vmName 端上虚拟机名
     * @param endIp 端ip
     * @return
     */
    @GetMapping("/containerList")
    @ApiOperation("查询虚拟机 Docker 容器列表")
    @OperationLogDesc(module = "镜像管理", events = "查询虚拟机 Docker 容器列表")
    public ResponseEntity<List<String>> listContainers(@RequestParam("vmName") String vmName,
                                                       @RequestParam("endIp") String endIp) {
        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        System.out.println(vmInfo2);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String url = "http://" + endIp + sufixUrl;
        String command = "docker ps -a ";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                command
        );
        requestBody.put("commands", commands);

        Gson gson = new Gson();

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );

        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            // 将 JSON 字符串解析为一个 Map 对象
            Map<String, Object> resultMap = gson.fromJson(response.getBody(), Map.class);

            String responseBody;

            // 获取 output 字段的值
            responseBody = ((String) resultMap.get("output")).trim();
            // 处理响应字符串，将输出结果按行分割，并返回容器名称列表
            List<String> containers = Arrays.asList(responseBody.split("\n"));
            return ResponseEntity.ok(containers);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok(Collections.emptyList());
        }
    }


    /**
     *
     * @param containerId 容器id（通过容器列表查看，需要通过id删除）
     * @param vmName 端上虚拟机名
     * @param endIp 端ip
     * @return
     */
    @DeleteMapping("/deleteContainer")
    @ApiOperation("删除 Docker 容器")
    @OperationLogDesc(module = "镜像管理", events = "删除 Docker 容器")
    public ResponseEntity<String> deleteContainer(@RequestParam("containerId") String containerId,
                                                  @RequestParam("vmName") String vmName,
                                                  @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String stopContainerCommand = "docker stop " + containerId;
        String deleteContainerCommand = "docker rm " + containerId;
        String url = "http://" + endIp + sufixUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                stopContainerCommand,
                deleteContainerCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );
        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }

    }

    @PostMapping("/stopContainer")
    @ApiOperation("停止 Docker 容器")
    @OperationLogDesc(module = "镜像管理", events = "停止 Docker 容器")
    public ResponseEntity<String> stopContainer(@RequestParam("containerId") String containerId,
                                                  @RequestParam("vmName") String vmName,
                                                  @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String stopContainerCommand = "docker stop " + containerId;
        String url = "http://" + endIp + sufixUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                stopContainerCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );
        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }

    }

    @PostMapping("/startContainer")
    @ApiOperation("启动 Docker 容器")
    @OperationLogDesc(module = "镜像管理", events = "启动 Docker 容器")
    public ResponseEntity<String> startContainer(@RequestParam("containerId") String containerId,
                                                  @RequestParam("vmName") String vmName,
                                                  @RequestParam("endIp") String endIp) {

        QueryWrapper<VMInfo2> qw = new QueryWrapper<>();
        if (vmName != null && !vmName.equals("")) {
            qw.eq("name", vmName);
        }
        VMInfo2 vmInfo2 = vmService.getOne(qw);
        String userName = vmInfo2.getUsername();
        String userPassword = vmInfo2.getPasswd();
        String host = vmInfo2.getIp();
        String startContainerCommand = "docker start " + containerId;
        String url = "http://" + endIp + sufixUrl;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("host", host);
        requestBody.put("username", userName);
        requestBody.put("password", userPassword);
        List<String> commands = Arrays.asList(
                startContainerCommand
        );
        requestBody.put("commands", commands);

        // 发起请求
        ResponseEntity<String> response = new RestTemplate().exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                String.class
        );
        // 获取响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            // 处理请求失败情况
            return ResponseEntity.ok("fail");
        }

    }


}
