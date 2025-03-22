package com.sjgarciav.ollamarestapi.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

@RestController
public class AzureInquiryController {
    private static final Logger log = LoggerFactory.getLogger(AzureInquiryController.class);
    private final ChatClient chatClient;
    private final Function<AzureDataService.Request, AzureDataService.Response> azureFunction;
    private final Function<AzureDataService1.Request, AzureDataService1.Response> azureFunction1;  


    public AzureInquiryController(ChatClient.Builder chatClientBuilder,
            Function<AzureDataService.Request, AzureDataService.Response> azureFunction,
            Function<AzureDataService1.Request, AzureDataService1.Response> azureFunction1) {

        this.chatClient = chatClientBuilder
                .build();

        this.azureFunction = azureFunction;
        this.azureFunction1 = azureFunction1;
    }

    @GetMapping("/api/v1/azure-story")
    public String getAzureStoryDetails(@RequestParam String storyKey) {

        try {            
            // Fetch Azure story details
            AzureDataService.Response azureResponse = azureFunction.apply(new AzureDataService.Request(storyKey));

            // Generate test cases using the AI model based on acceptance criteria               

            String prompt = "Based on the following acceptance criteria, generate in json format: detailed test cases with Test Case ID, Steps, Test Data, Test Case Description, and Expected Result:\n\n"            
                    + azureResponse.acceptanceCriteria();

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI response: {}", aiResponse);
            return aiResponse;
        } catch (Exception e) {
            log.error("Error processing request for storyKey: {}", storyKey, e);
            return "Unable to process your request at this time.";
        }
    }

    @GetMapping("/api/v1/azure-testcase")
    public String createAzureTestCase(@RequestParam String storyKey) {

        try {

            // Fetch Azure story details
            AzureDataService.Response azureResponse = azureFunction.apply(new AzureDataService.Request(storyKey));

            // Get AI model generated tc based on acceptance criteria            

            String prompt = "Based on the following acceptance criteria, generate in json format: detailed test cases with Test Case ID, Steps, Test Data, Test Case Description, and Expected Result:\n\n"
                    + azureResponse.acceptanceCriteria();

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("AI response: {}", aiResponse);

            
            // Generate test cases using the AI model based on acceptance criteria
            String tcTitle = null;
            String tcResult = null;
            String tcData = null;
            StringBuilder tcSteps = new StringBuilder();
            StringBuilder result = new StringBuilder();
            String steps;         
            int ItemId=0;

            // Parse the AI response and extract title and steps
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(aiResponse);

                for (int j=0; j<rootNode.size(); j++) {                    
                    if (rootNode.get(j).has("Test Case Description")) {
                        tcTitle = rootNode.get(j).get("Test Case Description").asText();
                    }
                    if (rootNode.get(j).has("Expected Result")) {
                        tcResult = rootNode.get(j).get("Expected Result").asText();
                    }
                    if (rootNode.get(j).has("Test Data")) {
                        //tcData = rootNode.get(j).get("Test Data").asText();
                        tcData = rootNode.get(j).get("Test Data").toString();
                    } 

                    log.info("SG Test Result:", tcResult);
                    log.info("SG Test Data:", tcData);
                    


                    if (rootNode.get(j).has("Steps")) {                   
                        tcSteps.delete(0, tcSteps.length());                             
                        for (int k=0; k<rootNode.get(j).get("Steps").size(); k++) {
                            steps = rootNode.get(j).get("Steps").get(k).asText();                         
                            if (tcSteps.isEmpty()) {                                
                                tcSteps.append("<steps>");
                            }                            
                                //if first step with tcData as a description of the step                                                        
                                if ( k == 0 ){
                                    ItemId = 0;
                                    if (tcData != null) {                                        
                                        tcSteps.append("<step id=").append("\\\"").append(ItemId).append("\\\" type=").append("\\\"Action")
                                        .append("\\\"><parameterizedString isformatted=").append("\\\"true").append("\\\">").append("Test Data: ")
                                        .append(System.lineSeparator()).append(tcData.replaceAll("[^A-Za-z0-9]"," "))
                                        //.append(System.lineSeparator()).append(tcData.replace("\"", " ")).append(tcData.replace("\'", " "))
                                        .append("</parameterizedString><parameterizedString isformatted=").append("\\\"true").append("\\\">")
                                        .append("</parameterizedString></step>");    
                                        ItemId++;
                                        tcSteps.append("<step id=").append("\\\"").append(ItemId).append("\\\" type=").append("\\\"Action")
                                        .append("\\\"><parameterizedString isformatted=").append("\\\"true").append("\\\">").append(steps)
                                        .append("</parameterizedString><parameterizedString isformatted=").append("\\\"true").append("\\\">")
                                        .append("</parameterizedString></step>");   
                                    }
                                    else {                                       
                                        tcSteps.append("<step id=").append("\\\"").append(ItemId).append("\\\" type=").append("\\\"Action")
                                        .append("\\\"><parameterizedString isformatted=").append("\\\"true").append("\\\">").append(steps)
                                        .append("</parameterizedString><parameterizedString isformatted=").append("\\\"true").append("\\\">")
                                        .append("</parameterizedString></step>");     
                                    }                                        
                                }                                   
                                else
                                {
                                    //last step with step description and expected result
                                    if (k+1 == rootNode.get(j).get("Steps").size()){
                                        tcSteps.append("<step id=").append("\\\"").append(ItemId).append("\\\" type=").append("\\\"Action")
                                        .append("\\\"><parameterizedString isformatted=").append("\\\"true").append("\\\">").append(steps)
                                        .append("</parameterizedString><parameterizedString isformatted=").append("\\\"true").append("\\\">").append(tcResult)
                                        .append("</parameterizedString></step>");                                       
                                    }    
                                    //other steps only the step description 
                                    else{
                                        tcSteps.append("<step id=").append("\\\"").append(ItemId).append("\\\" type=").append("\\\"Action")
                                        .append("\\\"><parameterizedString isformatted=").append("\\\"true").append("\\\">").append(steps)
                                        .append("</parameterizedString><parameterizedString isformatted=").append("\\\"true").append("\\\">")
                                        .append("</parameterizedString></step>");                                         
                                    }    
                                }                                                                                                                                                                                     
                                ItemId++;                                                 
                        }
                        if (!tcSteps.isEmpty()) {
                            tcSteps.append("</steps>");
                        }  
                                              

                        log.info("Extracted Title: {}", tcTitle);
                        log.info("Extracted Steps: {}", tcSteps); 
                        
                        
                        
                         // Ensure title and steps are not null
                        if (tcTitle == null || tcSteps == null) {
                            throw new RuntimeException("Title or Steps not found in AI response");
                        }
                        

                        // Create Azure test case using the AI response and AzureDataService1 class                        
                        AzureDataService1.Response azureResponse1 = azureFunction1.apply(new AzureDataService1.Request(tcTitle, tcSteps.toString()));
                        
                        log.info("TC id: ", azureResponse1.id());
                        log.info("TC message: ", azureResponse1.message());

                        result.append("TC id").append(azureResponse1.id()).append("TC message").append(azureResponse1.message()).append(System.lineSeparator());
                                           
                    }
                    
                }                                        
            
            } catch (Exception e) {
                log.error("Error parsing AI response", e);
                throw new RuntimeException("Failed to parse AI response", e);
            }

            return result.toString();           
            //return "hola";

        } catch (Exception e) {
            log.error("Error processing request for storyKey: {}", storyKey, e);
            return "Unable to process your request at this time.";
        }
    }

    


}
