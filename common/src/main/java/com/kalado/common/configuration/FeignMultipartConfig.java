package com.kalado.common.configuration;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignMultipartConfig {

    @Bean
    @Primary
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new SpringEncoder(
                () -> new HttpMessageConverters(new RestTemplate().getMessageConverters())
        ));
    }
}