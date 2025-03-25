package com.sjgarciav.ollamarestapi.functions;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureApiProperties.class)
public class PromptConfig {
    
}
