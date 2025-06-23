package com.sjgarciav.ollamarestapi.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

@Validated
public class AzureDataServiceUpdateWorkItem implements Function<AzureDataServiceUpdateWorkItem.Request, AzureDataServiceUpdateWorkItem.Response> {

    private static final Logger log = LoggerFactory.getLogger(AzureDataServiceGetUs.class);
    private final AzureApiProperties azureApiProperties;   
    private final WebClient webClient;

    public AzureDataServiceUpdateWorkItem(AzureApiProperties azureProps) {
        this.azureApiProperties = azureProps;
        this.webClient = WebClient.builder()
                .baseUrl(azureApiProperties.getApiUrl())
                .defaultHeaders(headers -> headers.setBearerAuth(azureApiProperties.getApiToken()))
                .build();
    }

@Override
public Response apply(Request request) {

    try {
        log.info("Updating work item with description: {}", request.description());

        String updateWorkItemEndpoint = "/" + azureApiProperties.getProject() + "/_apis/wit/workitems/" + request.storyKey() + "?api-version=7.1";        

        String jsonPayload = request.description();

        log.info("Azure Update Work Item JSON Payload: {}", jsonPayload);

        String jsonResponse = webClient.patch()
                .uri(updateWorkItemEndpoint)
                .header("Content-Type", "application/json-patch+json")
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Log the full JSON response
        log.info("Full Azure Update Work Item JSON Response: {}", jsonResponse);

        // Create and return Response record
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(request.description());
        String updatedDescription = "";
        if (rootNode.isArray() && rootNode.size() > 0) {
            updatedDescription = rootNode.get(0).path("value").asText();
        } else if (rootNode.isObject()) {
            updatedDescription = rootNode.path("value").asText();
        } else {
            updatedDescription = rootNode.asText();
        }

        return new Response(request.storyKey(), "Work item updated successfully with this description: " + updatedDescription);

    } catch (WebClientResponseException e) {
        log.error("Error response from Azure API: {}", e.getResponseBodyAsString(), e);
        throw new RuntimeException("Failed to update Azure work item", e);
    } catch (Exception e) {
        log.error("Error updating Azure work item", e);
        throw new RuntimeException("Failed to update Azure work item", e);
    }
}


    public record Request(String storyKey, String description) {}
    public record Response(String storyKey, String message) {}
}
 
