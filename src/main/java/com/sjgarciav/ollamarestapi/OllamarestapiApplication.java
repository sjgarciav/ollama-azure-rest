package com.sjgarciav.ollamarestapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.sjgarciav.ollamarestapi.functions.AzureApiProperties;


@SpringBootApplication
@ComponentScan(basePackages = "com.sjgarciav.ollamarestapi")
@EnableConfigurationProperties(AzureApiProperties.class)
public class OllamarestapiApplication {
	public static void main(String[] args) {
		SpringApplication.run(OllamarestapiApplication.class, args);
	}

}
