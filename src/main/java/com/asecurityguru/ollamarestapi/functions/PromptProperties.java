package com.asecurityguru.ollamarestapi.functions;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "prompt")
public class PromptProperties {
    private String testCases;
    private String aCRecommendations;

    // Getters and Setters
    public String getTestCases() {
        return testCases;
    }

    public void setTestCases(String testCases) {
        this.testCases = testCases;
    }

    public String getACRecommendations() {
        return aCRecommendations;
    }

    public void setACRecommendations(String aCRecommendations) {
        this.aCRecommendations = aCRecommendations;
    }

    @Override
    public String toString() {
        return "PromptProperties{" +
                "testCases='" + testCases + '\'' +
                ", aCRecommendations='" + aCRecommendations + '\'' +
                '}';
    }


}


