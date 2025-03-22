package com.sjgarciav.ollamarestapi.functions;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure")
public class AzureApiProperties {
    
    private String apiUrl;
    private String apiToken;
    private String project;
    private String promptTestCases;
    private String promptACRecommendations;

    // Getters and Setters
    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPromptTestCases() {
        return promptTestCases;
    }

    public void setPromptTestCases(String promptTestCases) {
        this.promptTestCases = promptTestCases;
    }

    public String getPromptACRecommendations() {
        return promptACRecommendations;
    }

    public void setPromptACRecommendations(String promptACRecommendations) {
        this.promptACRecommendations = promptACRecommendations;
    }

    @Override
    public String toString() {
        return "AzureApiProperties{" +
                "project='" + project + '\'' +
                ", apiToken='" + apiToken + '\'' +
                ", apiUrl='" + apiUrl + '\'' +
                ", prompttestCases='" + promptTestCases + '\'' +
                ", promptaCRecommendations='" + promptACRecommendations + '\'' +
                '}';
    }




    
}

