package com.kalado.search;

import com.kalado.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableFeignClients(basePackages = "com.kalado.common.feign")
@SpringBootApplication(scanBasePackages = {"com.kalado.search", "com.kalado.common"})
@EnableEurekaClient
@Import(GlobalExceptionHandler.class)
public class SearchApplication {
  public static void main(String[] args) {
    SpringApplication.run(SearchApplication.class, args);
  }
}
