package com.kalado.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.kalado.common.feign")
@SpringBootApplication(scanBasePackages = {"com.kalado.reporting", "com.kalado.common"})
@EnableEurekaClient
public class ReportingApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReportingApplication.class, args);
  }
}
