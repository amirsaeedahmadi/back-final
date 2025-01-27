package com.kalado.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.kalado.common.feign")
@SpringBootApplication(scanBasePackages = {"com.kalado.search", "com.kalado.common"})
@EnableEurekaClient
public class SearchApplication {
  public static void main(String[] args) {
    SpringApplication.run(SearchApplication.class, args);
  }
}
