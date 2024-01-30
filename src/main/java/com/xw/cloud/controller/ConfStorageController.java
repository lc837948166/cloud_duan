package com.xw.cloud.controller;


import com.xw.cloud.inter.OperationLogDesc;
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
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileReader;
import java.io.IOException;
@CrossOrigin
@Controller
@RequestMapping("confstorage")
@Api(tags = "配置存储管理", description = "处理与Kubernetes配置存储相关的操作")
public class ConfStorageController {

  @Value("${k8s.config}")
  private String k8sConfig;
  @ApiOperation(value = "获取配置映射视图", notes = "返回配置映射视图的路径")
  @RequestMapping(value = "/configmap", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取配置映射视图")
  public String configmap(){
    return "confstorage/configmap";
  }


  @ApiOperation(value = "获取配置映射列表", notes = "返回所有配置映射的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回配置映射列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/configmap/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取配置映射列表")
  public ModelAndView getConfigmapList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listConfigMapForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }


  @ApiOperation(value = "获取持久化卷申请视图", notes = "返回持久化卷申请视图的路径")
  @RequestMapping(value = "/pvc", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取持久化卷申请视图")
  public String pvc(){
    return "confstorage/pvc";
  }


  @ApiOperation(value = "获取持久化卷申请列表", notes = "返回所有持久化卷申请的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回持久化卷申请列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/pvc/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取持久化卷申请列表")
  public ModelAndView getPvcList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listPersistentVolumeClaimForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }

  @ApiOperation(value = "获取密钥视图", notes = "返回密钥视图的路径")
  @RequestMapping(value = "/secret", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取密钥视图")
  public String secret(){
    return "confstorage/secret";
  }


  @ApiOperation(value = "获取密钥列表", notes = "返回所有密钥的列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功返回密钥信息列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/secret/list", method = RequestMethod.GET)
  @OperationLogDesc(module = "存储管理", events = "获取密钥列表")
  public ModelAndView getSecretList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listSecretForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }

}
