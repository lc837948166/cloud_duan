package com.xw.cloud.aspect;

import cn.hutool.core.date.DateTime;
import com.xw.cloud.bean.OperationLog;
import com.xw.cloud.inter.OperationLogDesc;
import com.xw.cloud.service.impl.OperationLogServiceImpl;
import groovy.util.logging.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;


@Aspect
@Component
@Slf4j
public class LoggerAspect{

    final ThreadLocal<OperationLog> logFastThreadLocal = new ThreadLocal<>();
    final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(LoggerAspect.class);
    @Autowired
    private OperationLogServiceImpl operationLogService;
    /**
     * 切入连接点，使用固定 controller层下的所有文件
     */
    @Pointcut(value = "execution(* com.xw.cloud.controller..*(..)))")
    public void logPointcut() {
    }

    /**
     * 请求前置通知
     */
    @Before("logPointcut()")
    public void beforLogger(JoinPoint joinPoint) {

        // 获取请求参数
        String params = Arrays.toString(joinPoint.getArgs());

        DateTime now = DateTime.now();

        log.info("--------请求前置日志输出开始--------");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();

        log.info("请求访问时间: {}", now);
        // 获取请求url
        String requestUrl = request.getRequestURL().toString();
        log.info("请求url: {}", requestUrl);
        // 获取method
        log.info("请求方式: {}", request.getMethod());
        log.info("请求参数列表: {}", params);
        // 验证请求方法是否带有操作日志注解
        Method signature = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperationLogDesc operationLogDesc = signature.getAnnotation(OperationLogDesc.class);
        if (operationLogDesc != null) {
            // 操作日志记录
            OperationLog operationLog = OperationLog.getInstance();
            operationLog.setAddTime(now);
            operationLog.setOperationModule(operationLogDesc.module());
            operationLog.setOperationEvents(operationLogDesc.events());
            operationLog.setOperationData(params);
            operationLog.setOperationUrl(requestUrl);
            logFastThreadLocal.set(operationLog);
        }
    }

    /**
     * 请求后置通知，请求完成会进入到这个方法
     *
     * @param result 响应结果json
     */
    @AfterReturning(value = "logPointcut()", returning = "result")
    public void afterReturningLogger(Object result) {

        // 程序运时间(毫秒)
        log.info("请求结束时间: {}", dateTimeFormatter.format(LocalDateTime.now()));

        log.info("--------后台管理请求后置日志输出完成--------");

        // 保存操作日志
        OperationLog operationLog = logFastThreadLocal.get();

        if (operationLog != null) {
            operationLog.setOperationStatus(true);
            // 调用具体的 service 保存到数据库中
            operationLogService.save(operationLog);
            operationLog.setOperationResult(result.toString());
            // 移除本地线程数据
            logFastThreadLocal.remove();
        }

    }


    /**
     * 异常通知，请求异常会进入到这个方法
     */
    @AfterThrowing(value = "logPointcut()", throwing = "throwable")
    public void throwingLogger(Throwable throwable) {

        log.error("ErrorMessage：请根据异常产生时间前往异常日志查看相关信息");
        log.error("--------后台管理请求异常日志输出完成--------");
        // 保存操作日志
        OperationLog operationLog = logFastThreadLocal.get();
        if (operationLog != null) {
            operationLog.setOperationStatus(false);
            String throwableStr = throwable.toString();
//            if(throwableStr.contains(":")){
//                throwableStr = throwableStr.substring(throwableStr.indexOf(":") + 1);
//            }
            System.out.println("---------------");
            System.out.println(throwableStr);
            System.out.println("throwable----------------------");
            operationLog.setOperationResult(throwableStr);
            System.out.println("-------------------");
            System.out.println(operationLog);
            System.out.println("------------------------");
            // 调用具体的 service 保存到数据库中
            operationLogService.save(operationLog);
            // 移除本地线程数据
            logFastThreadLocal.remove();
        }

    }

}

