package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcraft.jsch.*;
import com.xw.cloud.bean.ImageInfo;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.RequestInfo;
import com.xw.cloud.bean.VmInfo;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.impl.NodeServiceImpl;
import groovy.util.logging.Slf4j;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Controller
@CrossOrigin
@RequestMapping("/containerd")
@Api(tags = "Containerd 镜像管理", description = "管理 Kubernetes 容器中的镜像")
public class ContainerdImageController {
    @Autowired
    NodeServiceImpl nodeService;

    @ApiOperation(value = "查看镜像页面", notes = "返回容器化镜像页面的路径")
    @RequestMapping(value = "/images", method = RequestMethod.GET)
    @OperationLogDesc(module = "镜像管理", events = "查看镜像页面")
    public String container() {
        return "containerd/images";
    }


    @ApiOperation(value = "获取镜像列表", notes = "通过节点 IP、用户名和密码查询镜像列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "成功返回镜像列表"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @RequestMapping(value = "/images/list", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "镜像管理", events = "获取镜像列表")
    public ModelAndView getContainerdImages(@RequestParam String nodeName) throws IOException {
//    public ModelAndView getContainerdImages() throws IOException {

        ModelAndView modelAndView = new ModelAndView("jsonView");


        /**
         * 通过Kubernetes客户端获取镜像
         */

       /* InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();*/

/*
        // 指定节点名称
        String nodeName = "your-node-name";

        // 获取指定节点
        V1Node node = api.readNode(nodeName, null, null, null);

        // 获取节点的状态
        V1NodeStatus status = node.getStatus();

        // 获取节点的镜像列表
        List<V1ContainerImage> images = status.getImages();

        // 遍历每个镜像
        for (V1ContainerImage image : images) {
            // 输出镜像名称
            System.out.println(image.getNames());
        }

 */

        // 获取所有节点列表
        /*V1NodeList nodeList = null;
        try {
            nodeList = api.listNode(null,null, null, null, null, null, null, null, null, null);
            List<V1Node> nodes = nodeList.getItems();

            // 遍历每个节点
            for (V1Node node : nodes) {
                // 获取节点的状态
                V1NodeStatus status = node.getStatus();

                // 获取节点的镜像列表
                List<V1ContainerImage> images = status.getImages();


                // 遍历每个镜像
                for (V1ContainerImage image : images) {
                    System.out.println(image.getNames());
//                    System.out.println(image.getSizeBytes());

                    String imageName = image.getNames().get(0);
                    String imageName1 = image.getNames().get(1);
                    // 解析镜像摘要
                    String[] parts = imageName.split("@");
                    String digest = parts[1];

                    // 拆分镜像名称和标签
                    String[] parts1 = imageName1.split(":");
                    String name = parts1[0];
                    String tag = parts1[1];

                    //将信息放到imageList
                    List<Object> imageList = new ArrayList<>();

                    imageList.add(name);
                    imageList.add(digest);
                    imageList.add(tag);
                    imageList.add(image.getSizeBytes());

//                    modelAndView.addObject("imageList", imageList);

                    // 输出镜像名称
                    System.out.println("镜像名称: " + name);
                    // 输出镜像摘要
                    System.out.println("镜像摘要: " + digest);
                    // 输出镜像标签
                    System.out.println("镜像标签: " + tag);
                    // 输出镜像大小
                    System.out.println("镜像大小: " + image.getSizeBytes());

                }
            }

        } catch (ApiException e) {
            throw new RuntimeException(e);
        }/*


        /**
         * 通过JSch命令获取镜像列表
         */

//        String virtualMachineIp = "192.168.174.133";
//        String userName = "root";
//        String userPassword = "@wsad1234"; // 请替换为您的实际密码

        QueryWrapper<NodeInfo> qw = new QueryWrapper<>();
        qw.eq("nodeName", nodeName);
        NodeInfo nodeInfo = nodeService.getOne(qw);

        String virtualMachineIp = nodeInfo.getNodeIp();
        String userName = nodeInfo.getNodeUserName();
        String userPassword = nodeInfo.getNodeUserPasswd();


//        String virtualMachineIp = vmInfo.getVirtualMachineIp();
//        String userName = vmInfo.getUserName();
//        String userPassword = vmInfo.getUserPassword(); // 请替换为您的实际密码

        System.out.println(virtualMachineIp+ userName+ userPassword);

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
            ((ChannelExec) execChannel).setCommand("crictl images"); // 设置执行的命令
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

            /**
             * 构造`crictl`命令获取镜像列表
             */
            // 构造`crictl`命令
            /*String crictlCommand = "ssh root@192.168.174.133 crictl images";



            // 执行命令并获取输出
            Process process = Runtime.getRuntime().exec(crictlCommand);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 读取命令输出并拼接结果
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                result.append("命令执行失败");
            }*/


        } catch (IOException | JSchException e) {
            e.printStackTrace();
        }


        return modelAndView;

    }


    @ApiOperation(value = "上传镜像页面", notes = "返回上传镜像的页面路径")
    @RequestMapping(value = "/uploadImage", method = RequestMethod.GET)
    @OperationLogDesc(module = "镜像管理", events = "上传镜像页面")
    public String createPod() {
        return "containerd/uploadImage";
    }



    @ApiOperation(value = "上传并创建镜像", notes = "通过节点 IP、用户名和密码上传并创建镜像")
    @ApiResponses({
            @ApiResponse(code = 200, message = "镜像上传成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "镜像管理", events = "上传并创建镜像")
    public String uploadImage(@RequestParam("tarFile") MultipartFile tarFile,
                              @RequestParam("nodeName") String nodeName
                             ) throws IOException {

//        String virtualMachineIp = "192.168.174.133";
//        String userName = "root";
//        String userPassword = "@wsad1234";

//        String virtualMachineIp = vmInfo.getVirtualMachineIp();
//        String userName = vmInfo.getUserName();
//        String userPassword = vmInfo.getUserPassword();

        QueryWrapper<NodeInfo> qw = new QueryWrapper<>();
        qw.eq("nodeName", nodeName);
        NodeInfo nodeInfo = nodeService.getOne(qw);

        String virtualMachineIp = nodeInfo.getNodeIp();
        String userName = nodeInfo.getNodeUserName();
        String userPassword = nodeInfo.getNodeUserPasswd();

        Session session = null;
        Channel channel = null;


        try {
            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            String imagePath = "/opt/tar";

            // 检查目标文件夹是否存在，如果不存在则创建
            try {
                sftpChannel.ls(imagePath); // 尝试列出目录
            } catch (SftpException e) {
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    String[] dirs = imagePath.split("/");
                    String path = "";
                    for (String dir : dirs) {
                        if (!dir.isEmpty()) {
                            path += "/" + dir;
                            try {
                                sftpChannel.ls(path); // 尝试列出目录
                            } catch (SftpException ex) {
                                if (ex.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                                    sftpChannel.mkdir(path); // 目录不存在，创建目录
                                }
                            }
                        }
                    }
                }
            }

            // 传输文件
            sftpChannel.put(tarFile.getInputStream(), imagePath+ "/" + tarFile.getOriginalFilename());

            System.out.println(tarFile.getOriginalFilename());
            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("ctr -n=k8s.io  image import " + imagePath+ "/" + tarFile.getOriginalFilename()); // 设置执行的命令
            execChannel.setInputStream(null);
            ((ChannelExec) execChannel).setErrStream(System.err);
            InputStream in = execChannel.getInputStream();
            execChannel.connect();

            return "Image uploaded successfully";
        } catch (JSchException | SftpException e) {
            throw new RuntimeException("Failed to upload image", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        /*
        try {
            String uploadCommand = "scp " + tarFile +"root@192.168.174.133:/tar/opt/";
            String ctrCommand = "ssh root@192.168.174.133 ctr image import "+ tarFile.getOriginalFilename();

                System.out.println(tarFile.getOriginalFilename());

            String crictlCommand = "ssh root@192.168.174.133 crictl images";
            // 构造导入镜像的命令
            String importCommand = "ctr --address=ssh://" + username + ":" + password + "@" + virtualMachineIp +
                    " image import " + tarFile.getOriginalFilename();

            // 执行导入镜像的命令
            Process process = Runtime.getRuntime().exec(crictlCommand);

            // 等待命令执行完成
            int exitCode = process.waitFor();
        if (exitCode == 0) {
            return "Image imported successfully";
        } else {
            return "Failed to import image";
        }
        } catch (InterruptedException e) {
            // 处理中断异常
            Thread.currentThread().interrupt();
            throw new RuntimeException("Image import interrupted", e);
        }*/
    }



    @ApiOperation(value = "删除镜像", notes = "通过节点 IP、用户名和密码，以及镜像 ID 删除镜像")
    @ApiResponses({
            @ApiResponse(code = 200, message = "镜像删除成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @RequestMapping(value = "/deleteImage", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "镜像管理", events = "删除镜像")
    public String deletePod(@RequestParam("imageId") String imageId,
                            @RequestParam("nodeName") String nodeName) throws IOException, ApiException {

//        VmInfo vmInfo = requestInfo.getVmInfo();
//        ImageInfo imageInfo = requestInfo.getImageInfo();


//        String virtualMachineIp = "192.168.174.133";
//        String username = "root";
//        String password = "@wsad1234"; // 请替换为您的实际密码
//        String imageId = "80d28bedfe5de";

//        String virtualMachineIp = vmInfo.getVirtualMachineIp();
//        String userName = vmInfo.getUserName();
//        String userPassword = vmInfo.getUserPassword(); // 请替换为您的实际密码
//        String imageId = imageInfo.getImageId();


        QueryWrapper<NodeInfo> qw = new QueryWrapper<>();
        qw.eq("nodeName", nodeName);
        NodeInfo nodeInfo = nodeService.getOne(qw);

        String virtualMachineIp = nodeInfo.getNodeIp();
        String userName = nodeInfo.getNodeUserName();
        String userPassword = nodeInfo.getNodeUserPasswd();

        Session session = null;
        Channel channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(userName, virtualMachineIp, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(userPassword);
            session.connect();

            // 执行命令
            Channel execChannel = session.openChannel("exec");
            ((ChannelExec) execChannel).setCommand("crictl rmi " + imageId); // 设置执行的命令
            execChannel.setInputStream(null);
            ((ChannelExec) execChannel).setErrStream(System.err);
            InputStream in = execChannel.getInputStream();
            execChannel.connect();

            System.out.println("==================删除成功到这");
            return "Image deleted successfully";

        } catch (JSchException e) {
            throw new RuntimeException("Failed to upload image", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }


    }
}
