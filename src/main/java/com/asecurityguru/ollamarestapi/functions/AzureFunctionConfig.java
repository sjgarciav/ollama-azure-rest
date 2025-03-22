package com.asecurityguru.ollamarestapi.functions;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AzureFunctionConfig {

    private final AzureApiProperties azureApiProperties;

    public AzureFunctionConfig(AzureApiProperties azureApiProperties) {
        this.azureApiProperties = azureApiProperties;
    }

    @Bean
    @Description("Provides a function to fetch Azure story details and generate test cases.")
    public Function<AzureDataService.Request, AzureDataService.Response> azureFunction()  {
        // Validate Azure configuration properties
        validateAzureConfigProperties(azureApiProperties);

        // Create and return a new AzureDataService instance
        return new AzureDataService(azureApiProperties);
    }

    @Bean
    @Description("Provides a function to fetch create a test cases on Azure.")
    public Function<AzureDataService1.Request, AzureDataService1.Response> azureFunction1()  {
        // Validate Azure configuration properties
        validateAzureConfigProperties(azureApiProperties);

        // Create and return a new AzureDataService instance
        return new AzureDataService1(azureApiProperties);
    }

    private void validateAzureConfigProperties(AzureApiProperties properties) {
        if (properties.getApiUrl() == null || properties.getApiUrl().isEmpty()) {
            throw new IllegalArgumentException("Azure API URL must be provided.");
        }
        if (properties.getApiToken() == null || properties.getApiToken().isEmpty()) {
            throw new IllegalArgumentException("Azure API token must be provided.");
        }
        if (properties.getProject() == null || properties.getProject().isEmpty()) {
            throw new IllegalArgumentException("Azure project must be provided.");
        }
    }
}

 