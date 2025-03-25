package com.sjgarciav.ollamarestapi.functions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PromptDataServiceGetProp {
    private final PromptProperties promptProperties;

    @Autowired
    public PromptDataServiceGetProp(PromptProperties promptProperties) {
        this.promptProperties = promptProperties;
    }

    public String getTestCases () {
        return promptProperties.getTestCases(); 
    }   

    public String getACRecommendations() {
        return promptProperties.getACRecommendations();        
    }       
}