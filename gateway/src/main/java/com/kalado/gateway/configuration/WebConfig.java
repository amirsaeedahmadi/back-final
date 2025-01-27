package com.kalado.gateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.kalado.gateway.exception.ExceptionHandlerAdvice;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.servlet.multipart.max-file-size:1MB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:10MB}")
    private String maxRequestSize;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules();
    }

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new FeignClientErrorDecoder(objectMapper);
    }

    @Bean
    public ExceptionHandlerAdvice exceptionHandlerAdvice(ObjectMapper objectMapper) {
        return new ExceptionHandlerAdvice(objectMapper);
    }

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins("http://localhost:8080", "http://kaladoshop.com/")
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
    //             .allowedHeaders("*")
    //             .exposedHeaders("Content-Disposition")
    //             .maxAge(3600);
    // }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            // .addMapping("/**")

            // // .allowedOrigins("*")
            // .allowedOrigins("http://localhost:8080", "http://kaladoshop.com")



            // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

            // .allowedHeaders("*")

            // .allowCredentials(true)

            // .maxAge(3600);
        .addMapping("/**")

        .allowedOrigins(
            "http://localhost:8080",  
            "http://kaladoshop.com/"
        )

        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

        .allowedHeaders("*")

        .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/")
                .setCachePeriod(3600);
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    private long parseSize(String size) {
        size = size.toUpperCase();
        if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "").trim()) * 1024;
        }
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "").trim()) * 1024 * 1024;
        }
        return Long.parseLong(size);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
    }
}