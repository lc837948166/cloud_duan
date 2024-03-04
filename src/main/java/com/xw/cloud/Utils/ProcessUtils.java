package com.xw.cloud.Utils;

import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProcessUtils {

    public String dockerRun() throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        /**
         * @param imageName 镜像名（通过镜像列表查看到到的，例如mysql:5.7,  镜像名：标签）
         * @param vmName 端上虚拟机名
         * @param endIp 端节点ip
         * @return
         */
        String apiUrl = "http://39.101.136.242:8080/docker/run";
        String params = "imageName=k8s.gcr.io/pause:3.2&vmName=test1&endIp=192.168.194.142"; // 要传递的参数
        // 构建URL对象
        URL url = new URL(apiUrl + "?" + params);
        // 创建HTTP连接
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        // 发送请求并获取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 处理响应
            return "成功调用API：" + response.toString();
        } else {
            return "调用API失败，响应码：" + responseCode;
        }
    }
    public String importImage(String imageFileName, String vmName, String endIp) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        /**
         * 如果运行docker镜像，调用执行命令接口，虚拟机安装docker ，端节点启动 executeComand-0.0.1-SNAPSHOT.jar
         * @param imageFileName 镜像文件名（例如mysql57.tar）
         * @param vmName 虚拟机名
         * @param targetPath 端上虚拟机中存放镜像路径
         * @param endIp 端节点 IP
         * @return
         */
        String apiUrl = "http://39.101.136.242:8080/docker/import";
        String params = "imageFileName=" + imageFileName + "&vmName=" + vmName + "&targetPath=/etc/usr/xwfiles/&endIp=" + endIp; // 要传递的参数
        // 构建URL对象
        URL url = new URL(apiUrl + "?" + params);
        // 创建HTTP连接
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(60000*10);
        conn.setReadTimeout(60000*10);
        // 发送请求并获取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 处理响应
            return  responseCode+response.toString();
        } else {
            return "" + responseCode;
        }
    }

    public  String uploadDockerToVM(String fileName,String vmName,String endIp,boolean flag) throws Exception{
        HttpURLConnection conn = null;
        BufferedReader reader = null;
            /**
             * 端节点传文件到创建的虚拟机
             * fileName:需要传的文件名
             * vmName:虚拟机名
             * targetPath:虚拟机存放文件路径
             * endIp:端节点的IP
             * sourceIp:云节点IP  39.101.136.242
             * 端节点 需要安装sshpash   https://blog.csdn.net/michaelwoshi/article/details/108902192
             */
            String apiUrl = "http://39.101.136.242:8080/docker/upload1";
            String params = "fileName="+fileName+"&vmName="+vmName+"&targetPath=/etc/usr/xwfiles&endIp="+endIp+"&flag="+flag; // 要传递的参数
            // 构建URL对象
            System.out.println(params);
            URL url = new URL(apiUrl + "?" + params);
            // 创建HTTP连接
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10*60000);
            conn.setReadTimeout(10*60000);
            // 发送请求并获取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                // 处理响应
                return  responseCode+response.toString();
            } else {
                return  ""+responseCode;
            }
    }

    public  String dispenseImgByIP(String sourceip,String fileName ,String endip) throws Exception{
        HttpURLConnection conn = null;
        BufferedReader reader = null;
//            http://39.98.124.97:8080/addVirtual?ImgName=TinyCore-current.iso&name=Tiny&memory=2&cpuNum=1&OStype=X86&nettype=bridge&serverip=undefined
            /**
             * 云节点传文件到端
             * 端节点用户名密码  root  3clear#6697
             *    创建文件夹 /etc/usr/xwfiles
             *    sourceip：39.98.124.97
             *    fileName:docker镜像名 或者程序包名  存在云节点文件夹中
             *    endip:端节点IP
             */
            String apiUrl = "http://39.101.136.242:8081/api/ssh/dispenseImgByIP";
            String params = "sourceip="+sourceip+"&fileName="+fileName+"&endip="+endip; // 要传递的参数
            // 构建URL对象
            URL url = new URL(apiUrl + "?" + params);
            // 创建HTTP连接
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(100000);
            conn.setReadTimeout(100000);
            // 发送请求并获取响应
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                // 处理响应
                return  responseCode+response.toString();
            } else {
                System.out.println("镜像下发失败");
                return  ""+responseCode;
            }
    }
    public  String createVM(String ImgName, String name, Integer memory,Integer cpuNum,String OStype,String  nettype,String serverip,String usetype,Integer bw) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
//            http://39.98.124.97:8080/addVirtual?ImgName=TinyCore-current.iso&name=Tiny&memory=2&cpuNum=1&OStype=X86&nettype=bridge&serverip=undefined
        String apiUrl = "http://"+serverip+":8080/addVirtual";
        /**
         * 云节点文件存放可以使用的镜像 ImgName 是镜像名
         * name:创建的虚拟机名
         * memory:内存
         * cpuNum:CPU数
         * OSType:操作系统类型 x86 or arm
         * nettype: 在云上创用nat，在端上用桥接
         * serverip: 端节点的IP，虚拟机的宿主机的IP
         */
        String newServerIp = serverip;
        if(serverip.equals("39.101.136.242")){
            newServerIp = "192.168.194.142";
        }
        String params = "ImgName=" + ImgName + "&name=" + name + "&memory=" + memory + "&cpuNum=" + cpuNum + "&OStype=" + OStype + "&nettype=" + nettype + "&serverip=" + newServerIp+"&usetype="+usetype; // 要传递的参数
        if(bw>=0){
            params += "&bandwidth="+bw;
        }
        System.out.println(params);
        // 构建URL对象
        URL url = new URL(apiUrl + "?" + params);
        System.out.println(url);
        // 创建HTTP连接
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30*60000);
        conn.setReadTimeout(30*60000);
        // 发送请求并获取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 处理响应
            return  responseCode+response.toString();
        } else {
            return  ""+responseCode;
        }
    }

    public  String deleteVM(String vmName,String serverip) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
//            http://39.98.124.97:8080/addVirtual?ImgName=TinyCore-current.iso&name=Tiny&memory=2&cpuNum=1&OStype=X86&nettype=bridge&serverip=undefined
        String apiUrl = "http://"+serverip+":8080/delete"+"/"+vmName;

        String params = "name=" + vmName; // 要传递的参数
        System.out.println(params);
        // 构建URL对象
        URL url = new URL(apiUrl);
        System.out.println(url);
        // 创建HTTP连接
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(100000);
        conn.setReadTimeout(100000);
        // 发送请求并获取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 处理响应
            return  responseCode+response.toString();
        } else {
            return  ""+responseCode;
        }
    }

    public String changeVM(String serverip, String vmName,int cpu, int memory) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
//            http://39.98.124.97:8080/addVirtual?ImgName=TinyCore-current.iso&name=Tiny&memory=2&cpuNum=1&OStype=X86&nettype=bridge&serverip=undefined
        String apiUrl = "http://"+serverip+":8080/changeVM"+"/"+vmName;

        String params = "memory=" + memory+"&"+"cpuNum="+cpu; // 要传递的参数
        // 构建URL对象
        URL url = new URL(apiUrl + "?" + params);
        System.out.println(url);
        // 创建HTTP连接
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5*60000);
        conn.setReadTimeout(5*60000);
        // 发送请求并获取响应
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // 处理响应
            return  responseCode+response.toString();
        } else {
            return  ""+responseCode;
        }
    }

}
