package com.xw.cloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@MapperScan("com.xw.cloud.mapper")
@SpringBootApplication
@EnableOpenApi
public class LibvirtApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibvirtApplication.class, args);
	}
}
