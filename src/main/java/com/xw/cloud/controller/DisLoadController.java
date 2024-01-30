package com.xw.cloud.controller;

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
import io.kubernetes.client.openapi.apis.*;
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
@Api(tags = "Kubernetes 分布式负载管理", description = "管理 Kubernetes 集群中的 Ingress 和 Service")
@CrossOrigin
@Controller
@RequestMapping("/disload")
public class DisLoadController {

  @Value("${k8s.config}")
  private String k8sConfig;

  @ApiOperation(value = "查看 Ingress 页面", notes = "返回展示 Ingress 资源的页面路径")
  @RequestMapping(value = "/ingress", method = RequestMethod.GET)
  public String ingress(){
    return "disload/ingress";
  }



  @ApiOperation(value = "获取 Ingress 列表", notes = "获取 Kubernetes 集群中所有命名空间的 Ingress 列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功获取 Ingress 列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/ingress/list", method = RequestMethod.GET)
  public ModelAndView getIngressList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    NetworkingV1Api exV1Api = new NetworkingV1Api();

    Call call = exV1Api.listIngressForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }


  @ApiOperation(value = "查看 Service 页面", notes = "返回展示 Service 资源的页面路径")
  @RequestMapping(value = "/service", method = RequestMethod.GET)
  public String service(){
    return "disload/service";
  }

  @ApiOperation(value = "获取 Service 列表", notes = "获取 Kubernetes 集群中所有命名空间的 Service 列表")
  @ApiResponses({
          @ApiResponse(code = 200, message = "成功获取 Service 列表"),
          @ApiResponse(code = 500, message = "服务器错误")
  })
  @RequestMapping(value = "/service/list", method = RequestMethod.GET)
  public ModelAndView getServiceList() throws IOException, ApiException {
    ModelAndView modelAndView = new ModelAndView("jsonView");
    String kubeConfigPath = ResourceUtils.getURL(k8sConfig).getPath();
    ApiClient client =
            ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
    Configuration.setDefaultApiClient(client);

    CoreV1Api api = new CoreV1Api();

    Call call = api.listServiceForAllNamespacesCall(null,null, null, null, null, null, null, null, 5, null,null);



    Response response = call.execute();

    if (!response.isSuccessful()) {
      modelAndView.addObject("result", "error!");
      return modelAndView;
    }

    modelAndView.addObject("result",response.body().string());

    return modelAndView;
  }
}
