package com.xw.cloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xw.cloud.inter.OperationLogDesc;
import io.kubernetes.client.openapi.models.V1DeleteOptions;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import okhttp3.*;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.BatchV1beta1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;

import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import java.io.InputStream;

import okhttp3.Response;
import com.xw.cloud.bean.ContainerInfo;
import com.xw.cloud.bean.PodInfo;
import com.xw.cloud.bean.PvcInfo;
import com.xw.cloud.bean.RequestInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Api(tags= "工作负载管理", description = "控制器用于管理 Kubernetes 工作负载，包括 Pods、Deployments、StatefulSets 等")
@Controller
@CrossOrigin
@RequestMapping("workload")
public class WorkloadController {
    @Value("${k8s.config}")
    private String k8sConfig;


    @ApiOperation(value = "获取命名空间页面", notes = "返回用于展示 Kubernetes 命名空间信息的页面。")
    @RequestMapping(value = "/namespace", method = RequestMethod.GET)
    public String namespace(){
        return "workload/namespace";
    }

    @ApiOperation(value = "获取命名空间列表", notes = "获取 Kubernetes 集群中所有命名空间的列表。")
    @RequestMapping(value = "/namespace/list", method = RequestMethod.GET)
    public ModelAndView getNamespaceList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call call = api.listNamespaceCall(null,null, null, null, null, null, null, null, 5, null,null);

        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result",response.body().string());

        return modelAndView;
    }


    @ApiOperation(value = "获取节点页面", notes = "返回用于展示 Kubernetes 节点信息的页面。")
    @RequestMapping(value = "/node", method = RequestMethod.GET)
    public String node(){
        return "workload/node";
    }

    @ApiOperation(value = "获取节点列表", notes = "获取 Kubernetes 集群中所有节点的列表。")
    @RequestMapping(value = "/node/list", method = RequestMethod.GET)
    public ModelAndView getNodeList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        Call call = api.listNodeCall(null,null, null, null, null, null, null, null, 5, null,null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result",response.body().string());

        return modelAndView;
    }

    @RequestMapping(value = "/getVmList", method = RequestMethod.GET)
    public String Vm() {
        return "workload/getVmList";
    }


    @ApiOperation(value = "获取虚拟机列表", notes = "获取 Kubernetes 集群中的所有虚拟机列表。")
    @RequestMapping(value = "/getVmList/list", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getVm() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");

        try (InputStream inputStream = getClass().getResourceAsStream("/k8s/vm.json")) {
            // 使用 try-with-resources 来确保文件流在使用完毕后被正确关闭

            if (inputStream == null) {
                throw new FileNotFoundException("JSON 文件未找到");
            }

            // 使用 BufferedReader 包装输入流以便逐行读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonContent = jsonBuilder.toString();

            // 在这里处理读取到的 JSON 内容
            System.out.println(jsonContent);

            modelAndView.addObject("vmList", jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelAndView;

    }

    @ApiOperation(value = "获取虚拟机名称列表", notes = "从 JSON 文件中获取所有虚拟机的名称列表。")
    @RequestMapping(value = "/getVmList/getVmNameList", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getVmNameList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");

        try (InputStream inputStream = getClass().getResourceAsStream("/k8s/vm.json")) {
            // 使用 try-with-resources 来确保文件流在使用完毕后被正确关闭

            if (inputStream == null) {
                throw new FileNotFoundException("JSON 文件未找到");
            }

            // 使用 BufferedReader 包装输入流以便逐行读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonContent = jsonBuilder.toString();

            // 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 字符串解析为对象数组
            Map<String, String>[] vmArray = objectMapper.readValue(jsonContent, Map[].class);

            // 创建只包含 "name" 属性的新数组
            String[] namesArray = new String[vmArray.length];
            for (int i = 0; i < vmArray.length; i++) {
                namesArray[i] = vmArray[i].get("name");
            }

            // 输出包含所有 "name" 属性值的数组
            System.out.println(Arrays.toString(namesArray));

            modelAndView.addObject("vmNameList", namesArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelAndView;

    }

    @ApiOperation(value = "根据名称获取虚拟机", notes = "根据提供的虚拟机名称从 JSON 文件中获取对应的虚拟机信息。")
    @RequestMapping(value = "/getVmList/getVmByName", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getVmByName(@RequestParam("name") String vmName) throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");

        try (InputStream inputStream = getClass().getResourceAsStream("/k8s/vm.json")) {
            // 使用 try-with-resources 来确保文件流在使用完毕后被正确关闭

            if (inputStream == null) {
                throw new FileNotFoundException("JSON 文件未找到");
            }

            // 使用 BufferedReader 包装输入流以便逐行读取
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonContent = jsonBuilder.toString();

            // 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 将 JSON 字符串解析为对象数组
            Map<String, Object>[] vmArray = objectMapper.readValue(jsonContent, Map[].class);

            // 遍历数组并根据 name 获取对应的对象
            for (Map<String, Object> vm : vmArray) {
                if (vm.get("name").equals(vmName)) {
                    System.out.println(vm); // 输出包含 name 为 "vm1" 的对象信息
                    modelAndView.addObject("vm", vm);
                    return modelAndView;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return modelAndView.addObject("error", "not find");

    }

    @ApiOperation(value = "获取定时任务页面", notes = "返回用于展示 Kubernetes 定时任务信息的页面。")
    @RequestMapping(value = "cronjob", method = RequestMethod.GET)
    public String cronjob() {
        return "workload/cronjob";
    }

    @ApiOperation(value = "获取定时任务列表", notes = "获取 Kubernetes 集群中所有定时任务的列表。")
    @RequestMapping(value = "cronjob/list", method = RequestMethod.GET)
    public ModelAndView getCronjobList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        BatchV1beta1Api api = new BatchV1beta1Api();

        Call call = api.listCronJobForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }


    @ApiOperation(value = "获取守护进程集页面", notes = "返回用于展示 Kubernetes 守护进程集信息的页面。")
    @RequestMapping(value = "/daemonset", method = RequestMethod.GET)
    public String daemonset() {
        return "workload/daemonset";
    }

    @ApiOperation(value = "获取守护进程集列表", notes = "获取 Kubernetes 集群中所有守护进程集的列表。")
    @RequestMapping(value = "/daemonset/list", method = RequestMethod.GET)
    public ModelAndView getDaemonsetList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api api = new AppsV1Api();

        Call call = api.listDaemonSetForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    @ApiOperation(value = "获取部署页面", notes = "返回用于展示 Kubernetes 部署信息的页面。")
    @RequestMapping(value = "/deployment", method = RequestMethod.GET)
    public String deployment() {
        return "workload/deployment";
    }

    @ApiOperation(value = "获取部署列表", notes = "获取 Kubernetes 集群中所有部署的列表。")
    @RequestMapping(value = "/deployment/list", method = RequestMethod.GET)
    public ModelAndView getDeploymentList() throws IOException, ApiException {


        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api api = new AppsV1Api();

        Call call = api.listDeploymentForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();
        System.out.print(response);
        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }


    @RequestMapping(value = "/createDeployment", method = RequestMethod.GET)
    public String createDeployment() {
        return "workload/createDeployment";
    }

    @ApiOperation(value = "创建部署", notes = "通过上传的 YAML 文件创建 Kubernetes 部署。")
    @RequestMapping(value = "/createDeployment", method = RequestMethod.POST)
    @ResponseBody
    public String createDeployment(@RequestParam("yamlFile") MultipartFile yamlFile) throws IOException, ApiException {
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
        Object obj = Yaml.load(yamlContent);


        if (obj instanceof V1Pod) {
            System.out.println("V1Pod");
            V1Pod pod = (V1Pod) obj;
            V1ObjectMeta metadata = pod.getMetadata();
            if (metadata != null) {
                String kind = pod.getKind();
                String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                switch (kind) {
                    case "Pod":
                        api.createNamespacedPod(namespace, pod, null, null, null);
                        break;
                    case "Deployment":
                        System.out.println("deployment类型qqqqq");
                        // 处理 Deployment 类型
                        appsApi.createNamespacedDeployment(namespace, (V1Deployment) obj, null, null, null);
                        break;
                    // ... 其他处理逻辑
                    // 添加其他资源类型的处理逻辑
                    default:
                        throw new IllegalArgumentException("Unknown resource type: " + kind);
                }
            }
        } else if (obj instanceof V1Deployment) {
            System.out.println("deployment类型");
            // 处理 Deployment 类型
            V1Deployment deployment = (V1Deployment) obj;
            V1ObjectMeta metadata = deployment.getMetadata();
            if (metadata != null) {
                String kind = deployment.getKind();
                String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                switch (kind) {
                    case "Pod":
//            api.createNamespacedPod(namespace, pod, null, null, null);
                        break;
                    case "Deployment":
                        appsApi.createNamespacedDeployment(namespace, deployment, null, null, null);
                        break;
                    // 处理其他资源类型的逻辑
                    default:
                        throw new IllegalArgumentException("Unknown resource type: " + kind);
                }
            }

        } else if (obj != null) {
            return "null";
        }

        return "Deployment created successfully.";
    }

    @ApiOperation(value = "获取作业页面", notes = "返回用于展示 Kubernetes 作业信息的页面。")
    @RequestMapping(value = "/job", method = RequestMethod.GET)
    public String job() {
        return "workload/job";
    }

    @ApiOperation(value = "获取作业列表", notes = "获取 Kubernetes 集群中所有作业的列表。")
    @RequestMapping(value = "/job/list", method = RequestMethod.GET)
    public ModelAndView getJobList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        BatchV1Api api = new BatchV1Api();

        Call call = api.listJobForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    @ApiOperation(value = "获取集群信息", notes = "获取 Kubernetes 集群的节点信息。")
    private void convertIntOrStringToString(V1PodList podList) {
        List<V1Pod> items = podList.getItems();
        for (V1Pod pod : items) {
            List<V1Container> containers = pod.getSpec().getContainers();
            for (V1Container container : containers) {
                V1Probe probe = container.getReadinessProbe();
                if (probe != null) {
                    io.kubernetes.client.openapi.models.V1HTTPGetAction httpGet = probe.getHttpGet();
                    if (httpGet != null) {
                        IntOrString port = httpGet.getPort();
                        if (port != null) {
                            if (port.getStrValue() == null && port.getIntValue() != null) {
                                // 将整数类型转换为字符串类型
                                String stringValue = String.valueOf(port.getIntValue());
                                httpGet.setPort(new IntOrString(stringValue));
                            }
                        }
                    }
                }
            }
        }
    }


//  @RequestMapping(value = "/pod", method = RequestMethod.GET)
//  public String pod(){
//    return "workload/pod";
//  }


    @RequestMapping(value = "/getNodeList", method = RequestMethod.GET)
    @OperationLogDesc(module = "容器管理", events = "获取节点")
    public ModelAndView getNode() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("workload/getPodList");

        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        try {
            // 获取集群节点信息
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);

            // 处理节点信息
            for (V1Node node : nodeList.getItems()) {
                System.out.println("Node Name: " + node.getMetadata().getName());
                // 可以进一步处理其他节点信息，比如标签、状态等
            }

            // 获取集群节点信息
            Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);
            Response response = call.execute();

            // 处理第二次请求的响应
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // 处理响应体，并将其添加到 ModelAndView 中
                modelAndView.addObject("result", responseBody);
            } else {
                // 处理请求失败情况
                modelAndView.addObject("result", "Error: " + response.message());
            }

        } catch (ApiException e) {
            // 处理异常情况
            System.err.println("Exception when calling CoreV1Api#listNode");
            e.printStackTrace();
        }

        return modelAndView;

    }

    @ApiOperation(value = "获取 Pod列表", notes = "从json中获取所有的pod。")
    @RequestMapping(value = "/getPodList", method = RequestMethod.GET)
    @OperationLogDesc(module = "容器管理", events = "获取容器列表")
    public ModelAndView getPod() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");

        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();


        V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);

        for (V1Pod pod : podList.getItems()) {
            if (pod.getMetadata().getAnnotations() == null || !pod.getMetadata().getAnnotations().containsKey("status")) {
                pod.getMetadata().setAnnotations(new HashMap<>());
                pod.getMetadata().getAnnotations().put("status", "Yes");
                api.replaceNamespacedPod(pod.getMetadata().getName(), pod.getMetadata().getNamespace(), pod, null, null, null);

            }

        }

        // 发起第二次请求并等待请求完成
        Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);
        Response response = call.execute();

        // 处理第二次请求的响应
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            // 处理响应体，并将其添加到 ModelAndView 中
            modelAndView.addObject("result", responseBody);
        } else {
            // 处理请求失败情况
            modelAndView.addObject("result", "Error: " + response.message());
        }


//        modelAndView.addObject("podList", podList.getItems());


        return modelAndView;
    }

//  @RequestMapping(value = "/getPodList", method = RequestMethod.GET)
//  public String Test(Model model) throws IOException, ApiException{
//    // 通过流读取，方式1
//    InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
//    // 使用 InputStream 和 InputStreamReader 读取配置文件
//    KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
//    Configuration.setDefaultApiClient(client);
//    CoreV1Api api = new CoreV1Api();
//
//
//    V1PodList podList = api.listPodForAllNamespaces(null,null, null, null, null, null, null, null, null,null );
//
//    for (V1Pod pod : podList.getItems()) {
//      if (pod.getMetadata().getAnnotations() == null || !pod.getMetadata().getAnnotations().containsKey("status")) {
//        pod.getMetadata().setAnnotations(new HashMap<>());
//        pod.getMetadata().getAnnotations().put("status", "Yes");
//      }
//
//    }
//
//    model.addAttribute("podList", podList.getItems());
//    return "workload/getPodList";
//  }



    @RequestMapping(value = "/editPod", method = RequestMethod.GET)
    public String editPod(@RequestParam("podName") String podName,
                          @RequestParam("podNamespace") String podNamespace,
                          Model model) {

        // 将接收到的值添加到 model 中
        model.addAttribute("podName", podName);
        model.addAttribute("podNamespace", podNamespace);
        return "workload/editPod";
    }

    @ApiOperation(value = "编辑 Pod", notes = "通过 Pod 名称和命名空间编辑 Pod。")
    @RequestMapping(value = "/editPod", method = RequestMethod.POST)
    @ResponseBody
//    public String editPod(@RequestParam("podName") String podName,
//                          @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    @OperationLogDesc(module = "容器管理", events = "迁移容器")
    public String editPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {
        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        String newNodeName = podinfo.getPodNodeName();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        try {
            // 获取Pod对象并确定其当前节点名称
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


            // 复制 Pod 对象
            V1Pod newPod = new V1Pod();
            newPod.setMetadata(new V1ObjectMeta());
            newPod.setSpec(new V1PodSpec());
            newPod.setStatus(new V1PodStatus());

            System.out.println(pod.getMetadata().getName());

            newPod.getMetadata().setName(pod.getMetadata().getName());
            newPod.getMetadata().setNamespace(pod.getMetadata().getNamespace());
            newPod.getMetadata().setLabels(pod.getMetadata().getLabels());
            newPod.getMetadata().setAnnotations(pod.getMetadata().getAnnotations());
            newPod.getSpec().setContainers(pod.getSpec().getContainers());
            newPod.getSpec().setVolumes(pod.getSpec().getVolumes());
            newPod.getSpec().setNodeName(newNodeName);
            newPod.setStatus(pod.getStatus());

            System.out.println("ready to delete...");

            // 删除当前 Pod
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            System.out.println("delete successfully");

            // 在新节点上创建 Pod

            Thread.sleep(1000);

//      System.out.println(newPod.getSpec().getNodeName());
//      System.out.println(newPod);
            api.createNamespacedPod(podNamespace, newPod, null, null, null);

            return "Successfully moved Pod ";
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return "Error: Failed to create Pod";
//        return "错误: 创建失败！";
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "配置容器镜像", notes = "通过 Pod 名称、命名空间和新的容器信息来配置镜像。")
    @RequestMapping(value = "/configureImage", method = RequestMethod.POST)
    @ResponseBody
//    public String configureImage(@RequestParam("podName") String podName,
//                                 @RequestParam("podNamespace") String podNamespace,
//                                 @RequestParam("containerName") String containerName,
//                                 @RequestParam("imageName") String imageName) throws IOException, ApiException {

    @OperationLogDesc(module = "容器管理", events = "配置镜像")
    public String configureImage(@RequestBody PodInfo podinfo) throws IOException, ApiException {
        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        List<ContainerInfo> containerInfoList = podinfo.getContainerInfoList();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        try {
            // 获取Pod对象并确定其当前节点名称
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


            // 新建 Pod 对象
            V1Pod newPod = new V1Pod();
            newPod.setMetadata(new V1ObjectMeta());
            newPod.setSpec(new V1PodSpec());
            newPod.setStatus(new V1PodStatus());

            // 复制 Pod 对象并添加container以配置image
            newPod.getMetadata().setName(pod.getMetadata().getName());
            newPod.getMetadata().setNamespace(pod.getMetadata().getNamespace());
            newPod.getMetadata().setLabels(pod.getMetadata().getLabels());
            newPod.getMetadata().setAnnotations(pod.getMetadata().getAnnotations());
            newPod.getSpec().setContainers(pod.getSpec().getContainers());
            newPod.getSpec().setVolumes(pod.getSpec().getVolumes());
            newPod.setStatus(pod.getStatus());


            for (ContainerInfo containerInfo : containerInfoList) {
                String containerName = containerInfo.getContainerName();
                String containerImage = containerInfo.getContainerImage();
                int port = containerInfo.getPort();

                // 处理每个 containerInfo 对象
                V1Container newContainer = new V1Container()
                        .name(containerName)
                        .image(containerImage)
                        .ports(Collections.singletonList(
                                new V1ContainerPort()
                                        .containerPort(port)));
                newPod.getSpec().getContainers().add(newContainer);
            }



            System.out.println(pod.getMetadata().getName());



            // 删除当前 Pod
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            // 在新节点上创建 Pod

            Thread.sleep(5000);

//      System.out.println(newPod);
            api.createNamespacedPod(podNamespace, newPod, null, null, null);

            return "Successfully configured Image ";
        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return "Error: Failed to configure Image";
//        return "错误: 创建失败！";
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @ApiOperation(value = "创建 Pod", notes = "通过 Pod 名称、命名空间、节点名和容器信息创建新的 Pod。")
    @RequestMapping(value = "/createPod", method = RequestMethod.GET)

    public String createPod() {
        return "workload/createPod";
    }

    @RequestMapping(value = "/createPod", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "容器管理", events = "创建容器")
    public String createPod(@RequestBody RequestInfo requestInfo) throws IOException, ApiException {

//
        PodInfo podinfo = requestInfo.getPodInfo();
        PvcInfo pvcInfo = requestInfo.getPvcInfo();
//        System.out.println("1222222222222222222222222");

        System.out.println(podinfo);
        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        String podNodeName = podinfo.getPodNodeName();
        List<ContainerInfo> containerInfoList = podinfo.getContainerInfoList();
        String pvcName = pvcInfo.getPvcName();


        System.out.println(podNamespace);

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
// 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
//    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        try {



            // 添加容器到Pod的规格中
            List<V1Container> containers = new ArrayList<>();

            for (ContainerInfo containerInfo : containerInfoList) {
                String containerName = containerInfo.getContainerName();
                String containerImage = containerInfo.getContainerImage();
                int port = containerInfo.getPort();

                port = 80;
                // 处理每个 containerInfo 对象...
                V1Container container = new V1Container()
                        .name(containerName)
                        .image(containerImage)
                        .ports(Collections.singletonList(
                                new V1ContainerPort()
                                        .containerPort(port)));
                containers.add(container);
            }



//            V1Container container1 = new V1Container()
//                    .name("test")
//                    .image("rancher/klipper-lb:v0.4.4")
//                    .ports(Collections.singletonList(
//                            new V1ContainerPort()
//                                    .containerPort(port)));

            // 将容器添加到容器列表中

//            containers.add(container1);


            // 创建 PVC
            V1PersistentVolumeClaimVolumeSource pvcVolumeSource = new V1PersistentVolumeClaimVolumeSource();
            pvcVolumeSource.setClaimName(pvcName);

            V1Volume pvcVolume = new V1Volume();
            pvcVolume.setName(pvcName);
            pvcVolume.setPersistentVolumeClaim(pvcVolumeSource);

            //创建spec
            V1PodSpec podSpec = new V1PodSpec()
                    .nodeName(podNodeName)
                    .volumes(Collections.singletonList(pvcVolume))
                    .containers(containers);

            //添加单个container
//      V1PodSpec podSpec = new V1PodSpec()
//              .containers(Collections.singletonList(container));

            //创建metadata
            V1ObjectMeta podMetadata = new V1ObjectMeta()
                    .namespace(podNamespace)
                    .name(podName);

            //创建pod
            V1Pod pod = new V1Pod()
                    .metadata(podMetadata)
                    .spec(podSpec);


            System.out.println("创建111111111111111");
            V1Pod createdPod = api.createNamespacedPod(podNamespace, pod, null, null, null);
            return "Pod created successfully.";

        } catch (ApiException e) {
            if (e.getCode() == 409) {
                // 发生冲突，返回失败响应给前端
                return "Error: Pod creation failed due to conflict";
//        return "错误: 重复创建！";
            } else {
                // 其他错误，返回失败响应给前端
                return e.getResponseBody();
//        return "错误: 创建失败！";
            }
        }


        //yaml文件创建
    /*String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
    Object obj = Yaml.load(yamlContent);



    if (obj instanceof V1Pod) {
      System.out.println("V1Pod");
      V1Pod pod = (V1Pod) obj;
      V1ObjectMeta metadata = pod.getMetadata();
      if (metadata != null) {
        String kind = pod.getKind();
        String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
        switch (kind) {
          case "Pod":
            api.createNamespacedPod(namespace, pod, null, null, null);

            break;

          default:
            throw new IllegalArgumentException("Unknown resource type: " + kind);
        }
      }
    } else if (obj instanceof V1Deployment) {
      System.out.println("deployment类型");
    // 处理 Deployment 类型
      V1Deployment deployment = (V1Deployment) obj;
      V1ObjectMeta metadata = deployment.getMetadata();
      if (metadata != null) {
        String kind = deployment.getKind();
        String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
        switch (kind) {
          case "Pod":
//            api.createNamespacedPod(namespace, pod, null, null, null);
            break;
          case "Deployment":
            appsApi.createNamespacedDeployment(namespace, deployment, null, null, null);
            break;
          // 处理其他资源类型的逻辑
          default:
            throw new IllegalArgumentException("Unknown resource type: " + kind);
        }
      }

  }else if (obj != null ){
      return "null";
    }
*/
//    return "Pod created successfully.";


//    return "Pod created successfully: " + createdPod.getMetadata().getName();
//    System.out.println("11111");
//    return "success";
    }

    @RequestMapping(value = "/pod", method = RequestMethod.GET)
    public String pod(Model model) throws IOException, ApiException {

        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList podList = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);

        System.out.println("1234345");
//    model.addAttribute("podList", podList.getItems());
        model.addAttribute("podList", podList.getItems());


//    convertIntOrStringToString(podList);
//     使用 Jackson ObjectMapper 将 List 转换为 JSON 字符串
//    ObjectMapper objectMapper = new ObjectMapper();
//    String jsonString = objectMapper.writeValueAsString(podList);
//
//    modelAndView.addObject("result",jsonString);
        return "workload/pod";
    }

    @ApiOperation(value = "删除 Pod", notes = "通过 Pod 名称和命名空间删除指定的 Pod。")
    @RequestMapping(value = "/deletePod", method = RequestMethod.POST)
    @ResponseBody
//    public String deletePod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    @OperationLogDesc(module = "容器管理", events = "删除容器")
    public String deletePod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        System.out.println("11111");


        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        V1DeleteOptions deleteOptions = new V1DeleteOptions();
        deleteOptions.setPropagationPolicy("Foreground");
        try {
            api.deleteNamespacedPod(podName, podNamespace, null, null, null, null, null, deleteOptions);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("删除 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod deleted successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to delete the Pod: " + podName + ", Error: " + e.getMessage();
        }
//    return "success";
    }

    @ApiOperation(value = "停止 Pod", notes = "通过 Pod 名称和命名空间来停止指定的 Pod。")
    @RequestMapping(value = "/stopPod", method = RequestMethod.POST)
    @ResponseBody
//    public String stopPod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    @OperationLogDesc(module = "容器管理", events = "停止容器")
    public String stopPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();

        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        try {
//      V1PodStatus newStatus = new V1PodStatus();
//      // 设置新的状态属性
//      newStatus.setPhase("Pending");
//      newStatus.setMessage("Pod is stopping successfully");


            // 获取Pod的当前状态
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);


//      System.out.println("------------------------------------------");
//      System.out.println(pod.getMetadata().getName());
//      String podLogs = api.readNamespacedPodLog(podName, podNamespace, null, null,null, null, null, null, null, null, null);
//      System.out.println(podLogs);


//      pod.setStatus(newStatus);

            // 检查Annotations是否为null
            if (pod.getMetadata().getAnnotations() == null) {
                pod.getMetadata().setAnnotations(new HashMap<>());
            }
            System.out.println("到这里");
            // 修改Pod的状态为Stopped
            pod.getMetadata().getAnnotations().put("status", "No");
            api.replaceNamespacedPod(podName, podNamespace, pod, null, null, null);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("停止 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod stopped successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to stop the Pod: " + podName + ", Error: " + e.getMessage();
        }
    }

    @ApiOperation(value = "启动 Pod", notes = "通过 Pod 名称和命名空间来启动指定的 Pod。")
    @RequestMapping(value = "/startPod", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "容器管理", events = "启动容器")
//    public String startPod(@RequestParam("podName") String podName, @RequestParam("podNamespace") String podNamespace) throws IOException, ApiException {
    public String startPod(@RequestBody PodInfo podinfo) throws IOException, ApiException {

        String podName = podinfo.getPodName();
        String podNamespace = podinfo.getPodNamespace();
        System.out.println("333333");

        long startTime = System.currentTimeMillis();

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        try {

            V1PodStatus newStatus = new V1PodStatus();
// 设置新的状态属性
            newStatus.setPhase("Running");
            newStatus.setMessage("Pod is running successfully");

            // 获取Pod的当前状态
            V1Pod pod = api.readNamespacedPod(podName, podNamespace, null);
            pod.setStatus(newStatus);

            if (pod.getMetadata().getAnnotations() == null) {
                pod.getMetadata().setAnnotations(new HashMap<>());
            }


            // 修改Pod的状态为Running
            pod.getMetadata().getAnnotations().put("status", "Yes");
            api.replaceNamespacedPod(podName, podNamespace, pod, null, null, null);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("启动 Pod 操作的执行时间为：" + executionTime + " 毫秒");

            return "Pod started successfully: " + podName;
        } catch (ApiException e) {
            return "Failed to start the Pod: " + podName + ", Error: " + e.getMessage();
        }
    }

    @ApiOperation(value = "删除部署", notes = "通过部署名称和命名空间来删除指定的 Kubernetes 部署。")
    @RequestMapping(value = "/deleteDeployment", method = RequestMethod.POST)
    @ResponseBody
    public String deleteDeployment(@RequestParam String deploymentName, @RequestParam String deploymentNamespace) throws IOException, ApiException {
//    String deploymentName = null;
//    String deploymentNamespace = podNamespace;

        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));

        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        // 删除 Deployment 中的所有 Pods
        try {



            deploymentName = "test-deployment"; // 替换成你的命名空间
            deploymentNamespace = "default"; // 替换成你的 Deployment 名称
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setPropagationPolicy("Foreground");

            System.out.println(deploymentName);
            System.out.println(deploymentNamespace);


            appsApi.deleteNamespacedDeployment(
                    deploymentName,
                    deploymentNamespace,
                    null,
                    null,
                    null,
                    null,
                    null,
                    deleteOptions
            );

            return "Deployment deleted successfully";
        } catch (ApiException e) {
            return "Exception when calling AppsV1Api#deleteNamespacedDeployment: " + e.getMessage();
        }
    }

    @ApiOperation(value = "获取 Pod 列表", notes = "获取当前 Kubernetes 集群中所有 Pod 的列表。")
    @RequestMapping(value = "/pod/list", method = RequestMethod.GET)
    public ModelAndView getPodList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call call = api.listPodForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);

//    V1PodList podList = api.listPodForAllNamespaces(null,null, null, null, null, null, null, null, null,null );
//    modelAndView.addObject("result",podList.getItems());

        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }


        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }
    @ApiOperation(value = "获取副本集页面", notes = "返回用于展示 Kubernetes 副本集信息的页面。")
    @RequestMapping(value = "/replicaset", method = RequestMethod.GET)
    public String replicaset() {
        return "workload/replicaset";
    }

    @ApiOperation(value = "获取副本集列表", notes = "获取 Kubernetes 集群中所有副本集的列表。")
    @RequestMapping(value = "/replicaset/list", method = RequestMethod.GET)
    public ModelAndView getReplicasetList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api api = new AppsV1Api();

        Call call = api.listReplicaSetForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    @ApiOperation(value = "获取复制控制器页面", notes = "返回用于展示 Kubernetes 复制控制器信息的页面。")
    @RequestMapping(value = "/replication", method = RequestMethod.GET)
    public String replication() {
        return "/workload/replication";
    }

    @ApiOperation(value = "获取复制控制器列表", notes = "获取 Kubernetes 集群中所有复制控制器的列表。")
    @RequestMapping(value = "/replication/list", method = RequestMethod.GET)
    public ModelAndView getReplicationList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call call = api.listReplicationControllerForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    @ApiOperation(value = "获取有状态集页面", notes = "返回用于展示 Kubernetes 有状态集信息的页面。")
    @RequestMapping(value = "/statefulset", method = RequestMethod.GET)
    public String statefulset() {
        return "workload/statefulset";
    }

    @ApiOperation(value = "获取有状态集列表", notes = "获取 Kubernetes 集群中所有有状态集的列表。")
    @RequestMapping(value = "/statefulset/list", method = RequestMethod.GET)
    public ModelAndView getStatefulsetList() throws IOException, ApiException {
        ModelAndView modelAndView = new ModelAndView("jsonView");
        String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
        Configuration.setDefaultApiClient(client);

        AppsV1Api api = new AppsV1Api();

        Call call = api.listStatefulSetForAllNamespacesCall(null, null, null, null, null, null, null, null, 5, null, null);


        Response response = call.execute();

        if (!response.isSuccessful()) {
            modelAndView.addObject("result", "error!");
            return modelAndView;
        }

        modelAndView.addObject("result", response.body().string());

        return modelAndView;
    }

    private class V1HTTPGetAction {
    }
}
