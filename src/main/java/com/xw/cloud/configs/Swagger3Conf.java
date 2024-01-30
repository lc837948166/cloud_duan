package com.xw.cloud.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.HashSet;

@Configuration //配置类
public class Swagger3Conf {
    @Bean
    public Docket createDocket() {
        return new Docket(DocumentationType.OAS_30)// 指定 Swagger3 版本号
                .apiInfo(createApiInfo());
    }

    @Bean
    public ApiInfo createApiInfo() {

        return new ApiInfo(
                "云资源管理系统API文档",
                "云资源管理系统API文档说明",
                "1.0.1",
                "懒得编.com",
                new Contact("清华",
                        "懒得编.com",
                        "懒得编@qq.com"
                ),
                "懒得编.license",
                "懒得编.com",
                new HashSet<>());
    }
}
