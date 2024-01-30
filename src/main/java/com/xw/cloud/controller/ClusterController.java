package com.xw.cloud.controller;
import com.xw.cloud.inter.OperationLogDesc;
import io.kubernetes.client.openapi.models.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import okhttp3.Call;
import okhttp3.Response;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.apis.StorageV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileReader;
import java.io.IOException;
@CrossOrigin
@Controller
@RequestMapping(value = "/cluster")
@Api(tags = "集群管理", description = "处理与Kubernetes集群相关的操作")
public class ClusterController {

  @Value("${k8s.config}")
  private String k8sConfig;

  @RequestMapping(value = "/getNamespaceList", method = RequestMethod.GET)
  public Model Test(Model model) throws IOException, ApiException{
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();
    V1NamespaceList namespaceList = api.listNamespace(null,null, null, null, null, null, null, null, null, null);

//    System.out.println("1234345");
//    model.addAttribute("podList", podList.getItems());
    model.addAttribute("namespaceList", namespaceList.getItems());

//    return "workload/getPodList";
    return model;
  }
  @ApiOperation(value = "获取命名空间视图", notes = "返回命名空间视图的路径")
  @RequestMapping(value = "/namespace", method = RequestMethod.GET)
  public String namespace(){
    return "cluster/namespace";
  }


  @ApiOperation(value = "获取命名空间列表", notes = "返回Kubernetes集群中所有命名空间的列表")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "成功获取命名空间列表"),
          @ApiResponse(code = 500, message = "服务器内部错误")
  })
  @RequestMapping(value = "/namespace/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取命名空间列表")
  public ModelAndView getNamespaceList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
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


  @ApiOperation(value = "获取节点视图", notes = "返回节点视图的路径")
  @OperationLogDesc(module = "集群管理", events = "获取节点视图")
  @RequestMapping(value = "/node", method = RequestMethod.GET)
  public String node(){
    return "cluster/node";
  }



  @ApiOperation(value = "获取节点列表", notes = "返回所有节点的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回节点列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/node/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取节点列表")
  public ModelAndView getNodeList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
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

  @ApiOperation(value = "获取持久卷视图", notes = "返回持久卷视图的路径")
  @RequestMapping(value = "/pv", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取持久卷视图")
  public String pv(){
    return "cluster/pv";
  }



  @ApiOperation(value = "获取持久卷列表", notes = "返回所有持久卷的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回持久卷列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/pv/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取持久卷列表")
  public ModelAndView getPvList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listPersistentVolumeCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }


  @ApiOperation(value = "获取角色视图", notes = "返回角色视图的路径")
  @RequestMapping(value = "/role", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取角色视图")
  public String role(){
    return "cluster/role";
  }


  @ApiOperation(value = "获取角色列表", notes = "返回所有角色的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回角色列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/role/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取角色列表")
  public ModelAndView getRoleList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);


    RbacAuthorizationV1Api api = new RbacAuthorizationV1Api();

    Call call = api.listRoleForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);
    Call callForCluster = api.listClusterRoleCall(null,null, null, null, null, null, null, null, 5, null,null);

    Response response = call.execute();
    Response responseForCluster = callForCluster.execute();

    if (!(response.isSuccessful() && responseForCluster.isSuccessful())) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());
    modelAndView.addObject("result2",responseForCluster.body().string());

    return modelAndView;
  }

  @ApiOperation(value = "获取存储类视图", notes = "返回存储类视图的路径")
  @RequestMapping(value = "/storageclass", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取存储类视图")
  public String storageclass(){
    return "cluster/storageclass";
  }



  @ApiOperation(value = "获取存储类列表", notes = "返回所有存储类的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回存储类列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/storageclass/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "集群管理", events = "获取存储类列表")
  public ModelAndView getStorageclassList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);


    StorageV1Api api = new StorageV1Api();

    Call call = api.listStorageClassCall(null,null, null, null, null, null, null, null, 5, null,null);

    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }


}
