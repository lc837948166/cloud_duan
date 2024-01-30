package com.xw.cloud.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xw.cloud.Utils.CommonResp;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.bean.PodLog;
import com.xw.cloud.bean.VMLog;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.resp.CommentResp;
import com.xw.cloud.service.impl.NodeServiceImpl;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeCondition;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.xml.soap.Node;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

@Api(tags = "节点管理", description = "管理节点")
@CrossOrigin
@Controller
@RequestMapping("node")
public class NodeController {

    @Autowired
    private NodeServiceImpl nodeService;

    @ApiOperation(value = "获取可调度节点列表", notes = "获取可调度节点列表")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "可调度节点列表查询")
    @GetMapping("/getNodeListIsSchedulable")
    public CommonResp getNodeListIsSchedulable() {
        QueryWrapper qw = new QueryWrapper<>();
        qw.eq("IsSchedulable",1);
        List<NodeInfo> nodeList = nodeService.list(qw);
        return new CommonResp(true, nodeList, "");
    }
    @ApiOperation(value = "获取节点列表", notes = "列出节点指标")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "节点列表查询")
    @GetMapping("/getNodeList1")
    public CommentResp getNodeList() {
        List<NodeInfo> nodeList = nodeService.list();
        System.out.println(nodeList);
        return new CommentResp(true, nodeList, "");
    }
    @ApiOperation(value = "添加节点列表", notes = "列出节点指标")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "节点列表添加")
    @PostMapping("/addNodeList1")
    public ResponseEntity<String> addNode(@RequestBody NodeInfo nodeInfo) throws IOException, ApiException, ParseException {
        if(nodeInfo.getNodeType().equals("端")){
            boolean ping = ping(nodeInfo.getNodeIp());
            if(ping) {
                nodeInfo.setNodeStatus("正常");
                nodeInfo.setNodeConnectivity(1);
            }
            else {
                nodeInfo.setNodeStatus("异常");
                nodeInfo.setNodeConnectivity(0);
            }
        }else {
            InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
            // 使用 InputStream 和 InputStreamReader 读取配置文件
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
            ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
            Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api();
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            //遍历所有的节点
            for (V1Node node : nodeList.getItems()) {
                String nodeName = node.getMetadata().getName();
                String nodeIP = node.getStatus().getAddresses().get(0).getAddress();
                // 获取节点状态
                List<V1NodeCondition> conditions = node.getStatus().getConditions();
                String nodeStatus = "异常";
                for (V1NodeCondition condition : conditions) {
                    String type = condition.getType();
                    String status = condition.getStatus();
                    System.out.println(type + " " +status);
                    if(type.equals("Ready")&& status.equals("True")){
                        nodeStatus = "正常";
                        break;
                    }
                }
                Integer nodeConnectivity = (Integer) (ping(nodeInfo.getNodeIp())?1:0);

                String creationTime = String.valueOf(node.getMetadata().getCreationTimestamp());
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsedDate = inputFormat.parse(creationTime);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date formattedDate = outputFormat.parse(outputFormat.format(parsedDate));
                // 判断节点是不是新加入的节点
                boolean found = false;
                if (nodeInfo.getNodeName().equals(nodeName) && nodeInfo.getNodeIp().equals(nodeIP)) {
                    found = true;
                }
                // 如果找到相同的节点
                if (found) {
                   nodeInfo.setNodeStatus(nodeStatus);
                   nodeInfo.setNodeConnectivity(nodeConnectivity);
                }
            }
        }
        boolean success = nodeService.save(nodeInfo);
        if (success) {
            return ResponseEntity.ok("Node added successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add node");
        }
    }


    @ApiOperation(value = "节点管理", notes = "更新节点")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "更新节点")
    @PostMapping("/update")
    public ResponseEntity<String> updateNode(@RequestBody NodeInfo nodeInfo) throws IOException, ApiException, ParseException {
        if(nodeInfo.getNodeType().equals("端")){
            boolean ping = ping(nodeInfo.getNodeIp());
            if(ping) {
                nodeInfo.setNodeStatus("正常");
                nodeInfo.setNodeConnectivity(1);
            }
            else{
                nodeInfo.setNodeStatus("异常");
                nodeInfo.setNodeConnectivity(0);
            }
        }else {
            InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
            // 使用 InputStream 和 InputStreamReader 读取配置文件
            KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
            ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
            Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api();
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            //遍历所有的节点
            for (V1Node node : nodeList.getItems()) {
                String nodeName = node.getMetadata().getName();
                String nodeIP = node.getStatus().getAddresses().get(0).getAddress();
                // 获取节点状态
                List<V1NodeCondition> conditions = node.getStatus().getConditions();
                String nodeStatus = "异常";
                for (V1NodeCondition condition : conditions) {
                    String type = condition.getType();
                    String status = condition.getStatus();
                    System.out.println(type + " " +status);
                    if(type.equals("Ready")&& status.equals("True")){
                        nodeStatus = "正常";
                        break;
                    }
                }
                Integer nodeConnectivity = (Integer) (ping(nodeInfo.getNodeIp())?1:0);

                String creationTime = String.valueOf(node.getMetadata().getCreationTimestamp());
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsedDate = inputFormat.parse(creationTime);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date formattedDate = outputFormat.parse(outputFormat.format(parsedDate));
                // 判断节点是不是新加入的节点
                boolean found = false;
                if (nodeInfo.getNodeName().equals(nodeName) && nodeInfo.getNodeIp().equals(nodeIP)) {
                    found = true;
                }
                // 如果找到相同的节点
                if (found) {
                    nodeInfo.setNodeStatus(nodeStatus);
                    nodeInfo.setNodeConnectivity(nodeConnectivity);
                }
            }
        }
        boolean success = nodeService.updateById(nodeInfo);
        if (success) {
            return ResponseEntity.ok("Node added successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update node");
        }
    }

    @ApiOperation(value = "删除", notes = "根据ID删除指定的节点")
    @DeleteMapping(value = "/deleteNodeList1/{id}")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "节点列表删除")
    public CommentResp deleteLog(@PathVariable Long id) {
        boolean b = nodeService.removeById(id);
        CommentResp com = new CommentResp(b,null,"");
        return com;
    }

    @ApiOperation(value = "ping", notes = "测试能否ping通")
    @ResponseBody
    @GetMapping("/ping")
    @OperationLogDesc(module = "节点管理", events = "ping通")
    public ResponseEntity<String> pingTest(@RequestParam("ip") String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean isReachable = address.isReachable(5000); // 5000表示超时时间，单位为毫秒
            QueryWrapper<NodeInfo> qw = new QueryWrapper<>();
            qw.eq("nodeIp", ip);
            NodeInfo nodeInfo = nodeService.getOne(qw);
            if (isReachable ) {
                nodeInfo.setNodeConnectivity(1);
                nodeService.updateById(nodeInfo);
                return ResponseEntity.ok(" IP are reachable");
            } else {
                nodeInfo.setNodeConnectivity(0);
                nodeService.updateById(nodeInfo);
                return ResponseEntity.ok(" IP are not reachable");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while performing the ping test: " + e.getMessage());
        }
    }

    private boolean ping(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);

            boolean isReachable = address.isReachable(5000); // 5000表示超时时间，单位为毫秒

            if (isReachable ) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

    }

    /**
     * 专用于更新创建时间、经纬度测试用
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/updateNodeList2")
    @ResponseBody
    public CommentResp updateNode1() throws IOException {
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        try {
            QueryWrapper<NodeInfo> qw = new QueryWrapper();
            qw.in("nodeType", "云", "边");
            List<NodeInfo> tNodeList = nodeService.list(qw);
            // 获取集群节点信息
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            for (V1Node node : nodeList.getItems()) {
                String nodeName = node.getMetadata().getName();
                String nodeIP = node.getStatus().getAddresses().get(0).getAddress();

                String creationTime = String.valueOf(node.getMetadata().getCreationTimestamp());

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date parsedDate = inputFormat.parse(creationTime);

                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date formattedDate = outputFormat.parse(outputFormat.format(parsedDate));

                Double nodeLon = 116.391276;//北京经度
                Double nodeLat = 39.906217;//北京纬度
                List<V1NodeCondition> conditions = node.getStatus().getConditions();
                String nodeStatus = "异常";
                for (V1NodeCondition condition : conditions) {
                    String type = condition.getType();
                    String status = condition.getStatus();
                    System.out.println(type + " " +status);
                    if(type.equals("Ready")&& status.equals("True")){
                        nodeStatus = "正常";
                        break;
                    }
                }



                for (NodeInfo tNode : tNodeList) {
                    if (tNode.getNodeName().equals(nodeName) && tNode.getNodeIp().equals(nodeIP)) {
                        tNode.setNodeCreateTime(formattedDate);
                        tNode.setNodeLon(nodeLon);
                        tNode.setNodeLat(nodeLat);
                        tNode.setNodeStatus(nodeStatus);
                        nodeService.updateById(tNode);
                        break;
                    }
                }

            }}
            catch(ApiException | ParseException e){
                // 处理异常情况
                System.err.println("Exception when calling CoreV1Api#listNode");
                e.printStackTrace();
            }
            CommentResp com = new CommentResp(true, null, "");
            return com;
    }

    /**
     * 更新节点信息
     * @return
     * @throws IOException
     */
    @ApiOperation(value = "更新", notes = "更新节点")
    @PostMapping(value = "/updateNodeList1")
    @ResponseBody
    @OperationLogDesc(module = "节点管理", events = "节点列表删除")
    public CommentResp updateNode() throws IOException {

        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        try {
            QueryWrapper<NodeInfo> qw = new QueryWrapper();
            qw.in("nodeType", "云","边");
            List<NodeInfo> tNodeList = nodeService.list(qw);
            // 获取集群节点信息
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            for (V1Node node : nodeList.getItems()) {
                String nodeName = node.getMetadata().getName();
                String nodeIP = node.getStatus().getAddresses().get(0).getAddress();
                // 获取节点状态
                List<V1NodeCondition> conditions = node.getStatus().getConditions();
                String nodeStatus = "异常";
                for (V1NodeCondition condition : conditions) {
                    String type = condition.getType();
                    String status = condition.getStatus();
                    System.out.println(type + " " +status);
                    if(type.equals("Ready")&& status.equals("True")){
                        nodeStatus = "正常";
                        break;
                    }
                }

                // 获取节点位置
                String nodeLocation = "北京";
                // 判断节点角色
                String nodeType ;
                Map<String, String> labels = node.getMetadata().getLabels();
                if (labels.containsKey("node-role.kubernetes.io/control-plane") || labels.containsKey("node-role.kubernetes.io/master")) {
                    nodeType = "云";
                } else {
                    nodeType = "边";
                }
                Integer nodeConnectivity = (Integer) (ping(nodeIP)?1:0);

                String nodeUserName = "root";
                String nodeUserPasswd = "@wsad1234";

                String creationTime = String.valueOf(node.getMetadata().getCreationTimestamp());

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date parsedDate = inputFormat.parse(creationTime);

                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date formattedDate = outputFormat.parse(outputFormat.format(parsedDate));

                System.out.println("11111111111111111111111111 " + formattedDate);

                Double nodeLon = 116.391276;//北京经度
                Double nodeLat = 39.906217;//北京纬度
                String bandwidth = "0";

                // 检查节点是否存在于tNodeList中
                boolean found = false;
                for (NodeInfo tNode : tNodeList) {
                    if (tNode.getNodeName().equals(nodeName) && tNode.getNodeIp().equals(nodeIP)) {
                        found = true;
                        break;
                    }
                }
                // 如果在tNodeList中找不到相同的节点名和节点IP
                if (!found) {
                    NodeInfo newNode = new NodeInfo(nodeName, nodeIP, nodeStatus, nodeLocation, nodeType,
                            nodeConnectivity, nodeUserName, nodeUserPasswd, formattedDate, nodeLon, nodeLat, nodeName, nodeIP, bandwidth  );
                    nodeService.save(newNode);
                }
            }

            // 遍历 tNodeList 中的每个节点信息
            for (NodeInfo node : tNodeList) {
                // 判断该节点信息是否在 nodeList 中存在
                boolean nodeExists = false;
                for (V1Node v1Node : nodeList.getItems()) {
                    String nodeName = v1Node.getMetadata().getName();
                    String nodeIP = v1Node.getStatus().getAddresses().get(0).getAddress();
                    if (node.getNodeName().equals(nodeName)
                            && node.getNodeIp().equals(nodeIP)) {
                        nodeExists = true;
                        break;
                    }
                }
                // 如果节点信息不在 nodeList 中存在，则从数据库中删除该节点信息
                if (!nodeExists) {
                    nodeService.removeById(node.getId());
                }
            }

        } catch (ApiException | ParseException e) {
            // 处理异常情况
            System.err.println("Exception when calling CoreV1Api#listNode");
            e.printStackTrace();
        }

//        boolean b = nodeService.updateById(nodeInfo);
        CommentResp com = new CommentResp(true,null,"");
        return com;
    }
}
