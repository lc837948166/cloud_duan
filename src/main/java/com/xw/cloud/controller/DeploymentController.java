package com.xw.cloud.controller;

import com.xw.cloud.inter.OperationLogDesc;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import okhttp3.Call;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.xw.cloud.bean.DeploymentInfo;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;


@CrossOrigin
@Controller
@RequestMapping("deployment")
@Api(tags = "Kubernetes 部署管理", description = "管理 Kubernetes 集群中的部署（Deployments）")
public class DeploymentController {
    @Value("${k8s.config}")
    private String k8sConfig;

    @Value("${k8s.token}")
    private String k8sToken;

    private static final String KUBERNETES_API_SERVER = "https://192.168.243.143:6443";

    @ApiOperation(value = "删除部署和服务", notes = "根据提供的部署名称删除对应的 Kubernetes 部署和服务")
    @ApiResponses({
            @ApiResponse(code = 200, message = "部署和服务删除成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/deleteDeployment", method = RequestMethod.POST)
    @OperationLogDesc(module = "部署管理", events = "删除部署和服务")
    public String deleteDeploymentAndService(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();
        try{
            // 删除 Deployment
            appsApi.deleteNamespacedDeployment(deploymentName, "default", null, null, null, null, null, null);

            // 删除 Service
            V1ServiceList serviceList = api.listNamespacedService("default", null, null, null, null, null, null, null, null, null, null);
            for (V1Service service : serviceList.getItems()) {
                if (deploymentName.equals(getServiceLabelValue(service, "app"))) {
                    api.deleteNamespacedService(service.getMetadata().getName(), "default", null, null, null, null, null, null);
                }
            }
            return "Deployment and Service deleted successfully.";
        }catch (ApiException e){
            return "Deployment and Service deleted failed.";
        }


    }


    @ApiOperation(value = "创建部署", notes = "从提供的 YAML 文件中创建 Kubernetes 部署")
    @ApiResponses({
            @ApiResponse(code = 200, message = "部署创建成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @RequestMapping(value = "/createDeployment", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "部署管理", events = "创建部署")
    public String createDeployment(@RequestParam("yamlFile") MultipartFile yamlFile) throws IOException, ApiException {
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();

        String yamlContent = new String(yamlFile.getBytes(), StandardCharsets.UTF_8);
        Iterable<Object> objects = Yaml.loadAll(yamlContent);

        for (Object obj : objects) {


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
                //V1Deployment testdeployment = new V1Deployment();
                V1Deployment deployment = (V1Deployment) obj;
                V1ObjectMeta metadata = deployment.getMetadata();
                //输出deployment的metadata
                System.out.println("---"+metadata);
                System.out.println("---"+deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
                if (metadata != null) {
                    String kind = deployment.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "Pod":
//            api.createNamespacedPod(namespace, pod, null, null, null);
                            break;
                        case "Deployment":
                            try{
                                appsApi.createNamespacedDeployment(namespace, deployment, null, null, null);
                            }catch (ApiException e){
                                System.out.println("Exception caught!");
                                System.out.println("Status code: " + e.getCode());
                                System.out.println("Response body: " + e.getResponseBody());
                                return "Deployment created failed.";
                            }
                            break;
                        // 处理其他资源类型的逻辑
                        default:
                            throw new IllegalArgumentException("Unknown resource type: " + kind);
                    }
                }

            }else if (obj instanceof V1Service) {
                // 处理 Service
                System.out.println("V1Service");
                V1Service service = (V1Service) obj;
                V1ObjectMeta metadata = service.getMetadata();
                if (metadata != null) {
                    String kind = service.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "Service":
                            api.createNamespacedService(namespace, service, null, null, null);
                            break;
                        // 处理其他资源类型的逻辑
                    }
                }
            } else if(obj instanceof V1PersistentVolumeClaim){
                //PersistentVolume
                System.out.println("V1PersistentVolumeClaim");
                V1PersistentVolumeClaim persistentVolumeClaim = (V1PersistentVolumeClaim) obj;
                V1ObjectMeta metadata = persistentVolumeClaim.getMetadata();
                if (metadata != null) {
                    String kind = persistentVolumeClaim.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "PersistentVolumeClaim":
                            api.createNamespacedPersistentVolumeClaim(namespace, persistentVolumeClaim, null, null, null);
                            break;
                        // 处理其他资源类型的逻辑
                    }
                }

            }else if (obj instanceof V1PersistentVolume ){
                //PersistentVolume
                System.out.println("V1PersistentVolume");
                V1PersistentVolume persistentVolume = (V1PersistentVolume) obj;
                V1ObjectMeta metadata = persistentVolume.getMetadata();
                if (metadata != null) {
                    String kind = persistentVolume.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "PersistentVolume":
                            api.createPersistentVolume(persistentVolume, null, null, null);
                            break;
                        // 处理其他资源类型的逻辑
                    }
                }
            } else if (obj instanceof V1StatefulSet) {
                // 处理StatefulSet
                System.out.println("V1StatefulSet");
                V1StatefulSet statefulSet = (V1StatefulSet) obj;
                V1ObjectMeta metadata = statefulSet.getMetadata();
                if (metadata != null) {
                    String kind = statefulSet.getKind();
                    String namespace = metadata.getNamespace() != null ? metadata.getNamespace() : "default";
                    switch (kind) {
                        case "StatefulSet":
                            appsApi.createNamespacedStatefulSet(namespace, statefulSet, null, null, null);
                            break;
                        // 处理其他资源类型的逻辑
                    }
                }

            } else if (obj != null) {
                return "null";
            }
        }

        return "Deployment created successfully.";
    }


    @ApiOperation(value = "获取部署列表", notes = "获取 Kubernetes 集群中所有命名空间的部署列表")
    @ApiResponses({
            @ApiResponse(code = 200, message = "成功获取部署列表"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @OperationLogDesc(module = "部署管理", events = "获取部署列表")
    public ModelAndView getDeploymentList() throws IOException, ApiException {

        ModelAndView modelAndView = new ModelAndView("jsonView");
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

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



    private String getServiceLabelValue(V1Service service, String labelKey) {
        if (service.getMetadata() != null && service.getMetadata().getLabels() != null) {
            return service.getMetadata().getLabels().get(labelKey);
        }
        return null;
    }


    @ApiOperation(value = "通过参数部署", notes = "根据提供的参数创建 Kubernetes 部署和服务")
    @ApiResponses({
            @ApiResponse(code = 200, message = "部署和服务创建成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @RequestMapping(value ="/deployByParam", method = RequestMethod.POST)
    @ResponseBody
    @OperationLogDesc(module = "部署管理", events = "通过参数部署")
    public String deploy(@RequestBody DeploymentInfo request) throws IOException, ApiException {

        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();
        String deploymentName = request.getDeploymentName();

        /*//初始化DeploymentInfo
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setDeploymentName("your-java-web-app5");
        deploymentInfo.setImage("zytest:1.1");
        deploymentInfo.setContainerPort(8080);
        deploymentInfo.setServicePort(80);
        deploymentInfo.setNodePort(30005);*/

        // 创建 Deployment 和 Service
        CoreV1Api api = new CoreV1Api();
        AppsV1Api appsApi = new AppsV1Api();
        try {
            createDeploymentByParam(request);
            createServiceByParam(request);
            return "Deployment and Service created successfully.";

        }catch (ApiException e){
            try {
                appsApi.deleteNamespacedDeployment(deploymentName, "default", null, null, null, null, null, null);

                // 删除 Service
                V1ServiceList serviceList = api.listNamespacedService("default", null, null, null, null, null, null, null, null, null, null);
                for (V1Service service : serviceList.getItems()) {
                    if (deploymentName.equals(getServiceLabelValue(service, "app"))) {
                        api.deleteNamespacedService(service.getMetadata().getName(), "default", null, null, null, null, null, null);
                    }
                }
            }catch (ApiException e1){
                System.out.println("Exception caught!");
                System.out.println("Status code: " + e1.getCode());
                System.out.println("Response body: " + e1.getResponseBody());
                e1.printStackTrace();
            }
            return "Deployment and Service created failed.";
        }



    }
    @CrossOrigin
    private void createDeploymentByParam(DeploymentInfo request) throws ApiException {
        AppsV1Api appsApi = new AppsV1Api();

        String deploymentName = request.getDeploymentName();

        // 构建 Deployment 对象
        V1Deployment deployment = new V1Deployment()
                .apiVersion("apps/v1")
                .kind("Deployment")
                .metadata(new V1ObjectMeta().name(deploymentName).labels(Collections.singletonMap("app", deploymentName)))
                .spec(new V1DeploymentSpec()
                        .replicas(1)
                        .selector(new V1LabelSelector().matchLabels(Collections.singletonMap("app", deploymentName)))
                        .template(new V1PodTemplateSpec()
                                .metadata(new V1ObjectMeta().labels(Collections.singletonMap("app", deploymentName)))
                                .spec(new V1PodSpec()
                                        .containers(Collections.singletonList(new V1Container()
                                                .name(deploymentName)
                                                .image(request.getImage())
                                                .ports(Collections.singletonList(new V1ContainerPort().containerPort(request.getContainerPort()))))))));


        // 创建 Deployment
        appsApi.createNamespacedDeployment("default", deployment, null, null, null);
    }
    @CrossOrigin
    private void createServiceByParam(DeploymentInfo request) throws ApiException {
        CoreV1Api coreApi = new CoreV1Api();

        String serviceName = request.getDeploymentName() + "-service";

        // 构建 Service 对象
        V1Service service = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta().name(serviceName).labels(Collections.singletonMap("app", request.getDeploymentName())))
                .spec(new V1ServiceSpec()
                        .selector(Collections.singletonMap("app", request.getDeploymentName()))
                        .type("NodePort")
                        .ports(Collections.singletonList(new V1ServicePort().port(request.getServicePort()).targetPort(new IntOrString(request.getContainerPort())).nodePort(request.getNodePort()))));
        // 创建 Service
        coreApi.createNamespacedService("default", service, null, null, null);
    }


    @ApiOperation(value = "停止部署", notes = "停止指定的 Kubernetes 部署")
    @ApiResponses({
            @ApiResponse(code = 200, message = "部署停止成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/stopDeployment", method = RequestMethod.GET)
    @OperationLogDesc(module = "部署管理", events = "停止部署")
    public String stopDeployment(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        AppsV1Api appsApi = new AppsV1Api();
        //修改k3s中Deployment的replicas为0
        //V1Patch patch = new V1Patch("{\"spec\":{\"replicas\":0}}");
        V1Patch patch = new V1Patch("[{ \"op\": \"replace\", \"path\": \"/spec/replicas\", \"value\": 0 }]");

        try {
            appsApi.patchNamespacedDeployment(deploymentName, "default", patch, null, null, null, null);
            return "Deployment stopped successfully.";
        } catch (ApiException e) {
            System.out.println("Exception caught!");
            System.out.println("Status code: " + e.getCode());
            System.out.println("Response body: " + e.getResponseBody());
            e.printStackTrace();
            return "Deployment stopped fail.";
        }





    }


    @ApiOperation(value = "启动部署", notes = "启动指定的 Kubernetes 部署")
    @ApiResponses({
            @ApiResponse(code = 200, message = "部署启动成功"),
            @ApiResponse(code = 500, message = "服务器错误")
    })
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/startDeployment", method = RequestMethod.GET)
    @OperationLogDesc(module = "部署管理", events = "启动部署")
    public String startDeploymentName(@RequestParam("deploymentName") String deploymentName) throws IOException, ApiException {
        // 通过流读取，方式1
        InputStream in1 = this.getClass().getResourceAsStream("/k8s/config");
        // 使用 InputStream 和 InputStreamReader 读取配置文件
        KubeConfig kubeConfig = KubeConfig.loadKubeConfig(new InputStreamReader(in1));
        ApiClient client = ClientBuilder.kubeconfig(kubeConfig).build();

        AppsV1Api appsApi = new AppsV1Api();
        //修改k3s中Deployment的replicas为0
        //V1Patch patch = new V1Patch("{\"spec\":{\"replicas\":0}}");
        V1Patch patch = new V1Patch("[{ \"op\": \"replace\", \"path\": \"/spec/replicas\", \"value\": 1 }]");

        try {
            appsApi.patchNamespacedDeployment(deploymentName, "default", patch, null, null, null, null);
            return "Deployment start successfully.";
        } catch (ApiException e) {
            System.out.println("Exception caught!");
            System.out.println("Status code: " + e.getCode());
            System.out.println("Response body: " + e.getResponseBody());
            e.printStackTrace();
            return "Deployment start fail.";
        }

    }





}
