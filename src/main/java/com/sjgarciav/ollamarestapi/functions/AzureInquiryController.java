package com.sjgarciav.ollamarestapi.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

@RestController
public class AzureInquiryController {
    private static final Logger log = LoggerFactory.getLogger(AzureInquiryController.class);
    private final ChatClient chatClient;
    private final Function<AzureDataServiceGetUs.Request, AzureDataServiceGetUs.Response> azureFunction;
    private final Function<AzureDataServiceCreateTc.Request, AzureDataServiceCreateTc.Response> azureFunction1;  
    private final Function<AzureDataServiceUpdateWorkItem.Request, AzureDataServiceUpdateWorkItem.Response> azureFunction2;
    private final com.sjgarciav.ollamarestapi.functions.PromptDataServiceGetProp promptService;


    public AzureInquiryController(ChatClient.Builder chatClientBuilder,
            Function<AzureDataServiceGetUs.Request, AzureDataServiceGetUs.Response> azureFunction,
            Function<AzureDataServiceCreateTc.Request, AzureDataServiceCreateTc.Response> azureFunction1,
            Function<AzureDataServiceUpdateWorkItem.Request, AzureDataServiceUpdateWorkItem.Response> azureFunction2,
            PromptDataServiceGetProp promptService) {

        this.chatClient = chatClientBuilder
                .build();

        this.azureFunction = azureFunction;
        this.azureFunction1 = azureFunction1;
        this.azureFunction2 = azureFunction2;
        this.promptService = promptService;
    }

    //rest that needs the azure story to generate the AI response with the test cases
    @GetMapping("/api/v1/azure-story-testcases")
    public String getAzureStoryDetails(@RequestParam String storyKey) {

        try {            
            // Fetch Azure story details
            AzureDataServiceGetUs.Response azureResponse = azureFunction.apply(new AzureDataServiceGetUs.Request(storyKey));

            // Generate test cases using the AI model based on acceptance criteria                     
            
            String prompt = promptService.getTestCases() + azureResponse.acceptanceCriteria();

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

    //rest that needs the azure story to generate incompletnes, misunderstandings and errors on the user story
    @GetMapping("/api/v1/azure-story-problems")
    public String getAzureStoryProblems(@RequestParam String storyKey) {

        try {            
            // Fetch Azure story details
            AzureDataServiceGetUs.Response azureResponse = azureFunction.apply(new AzureDataServiceGetUs.Request(storyKey));

            // Generate problems on user story                   
            
            String prompt = promptService.getACRecommendations() + azureResponse.acceptanceCriteria();

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

    //rest that needs the azure story to create the azure test cases
    @GetMapping("/api/v1/azure-create-testcases")
    public String createAzureTestCase(@RequestParam String storyKey) {

        try {

            // Fetch Azure story details
            AzureDataServiceGetUs.Response azureResponse = azureFunction.apply(new AzureDataServiceGetUs.Request(storyKey));

            // Get AI model generated tc based on acceptance criteria               

            String prompt = promptService.getTestCases() + azureResponse.acceptanceCriteria();

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
            String regex = "[\\\"'\\\n\\\r\\\t\\\b]";

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
                                        //.append(System.lineSeparator()).append(tcData.replaceAll("[^A-Za-z0-9:;.]"," "))
                                        .append(System.lineSeparator()).append(tcData.replaceAll(regex," "))                                        
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
                        AzureDataServiceCreateTc.Response azureResponse1 = azureFunction1.apply(new AzureDataServiceCreateTc.Request(tcTitle, tcSteps.toString()));
                        
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

    //rest that needs the azure story and the AI response to create the azure test cases
    @GetMapping("/api/v1/azure-create-testcases-bobyAIResponse")
    public String createAzureTestCase1(@RequestParam String storyKey, @RequestBody String aiResponse) {

        try {
            
            log.info("AI response: {}", aiResponse);
            
            // Generate test cases using the AI model based on acceptance criteria
            String tcTitle = null;
            String tcResult = null;
            String tcData = null;
            StringBuilder tcSteps = new StringBuilder();
            StringBuilder result = new StringBuilder();
            String steps;         
            int ItemId=0;    
            String regex = "[\\\"'\\\n\\\r\\\t\\\b]";    

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
                                        //.append(System.lineSeparator()).append(tcData.replaceAll("[^A-Za-z0-9:;.]"," "))
                                        .append(System.lineSeparator()).append(tcData.replaceAll(regex," "))  
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
                        AzureDataServiceCreateTc.Response azureResponse1 = azureFunction1.apply(new AzureDataServiceCreateTc.Request(tcTitle, tcSteps.toString()));
                        
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

        } catch (Exception e) {
            log.error("Error processing request for storyKey: {}", storyKey, e);
            return "Unable to process your request at this time.";
        }
    }

    //rest that needs the azure story to update the description of the user story
    @GetMapping("/api/v1/azure-update-workItem")
    public String updateAzureStory(@RequestParam String storyKey, @RequestBody String aiDescription) {
        StringBuilder result = new StringBuilder();
        try {
            log.info("Updating Azure story with key: {}", storyKey);
            log.info("New description: {}", aiDescription);

            // Call the Azure function to update the story
            AzureDataServiceUpdateWorkItem.Response azureResponse = azureFunction2.apply(new AzureDataServiceUpdateWorkItem.Request(storyKey, aiDescription));

            log.info("Work Item id: {}", azureResponse.storyKey());
            log.info("Work Item message: {}", azureResponse.message());

            result.append("Work Item id: ").append(azureResponse.storyKey()).append(System.lineSeparator());
            result.append("Work Item message: ").append(azureResponse.message()).append(System.lineSeparator());            

            return result.toString();
        } catch (Exception e) {
            log.error("Error updating Azure story", e);
            return "Failed to update Azure story.";
        }

    }
}
