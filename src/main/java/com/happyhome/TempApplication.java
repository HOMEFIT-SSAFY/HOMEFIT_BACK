package com.happyhome;

import com.happyhome.config.OpenApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(OpenApiProperties.class)
@EnableScheduling
public class TempApplication {

    public static void main(String[] args) {
        SpringApplication.run(TempApplication.class, args);
    }
}

