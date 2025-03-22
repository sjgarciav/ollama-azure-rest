package com.sjgarciav.ollamarestapi.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.function.Function;

@Validated
public class AzureDataService implements Function<AzureDataService.Request, AzureDataService.Response> {

    private static final Logger log = LoggerFactory.getLogger(AzureDataService.class);
    private final AzureApiProperties azureApiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;


    public AzureDataService(AzureApiProperties azureProps) {
        this.azureApiProperties = azureProps;
        this.webClient = WebClient.builder()
                .baseUrl(azureApiProperties.getApiUrl())
                .defaultHeaders(headers -> headers.setBearerAuth(azureApiProperties.getApiToken()))
                .build();
        
    }

@Override
public Response apply(Request request) {
    try {    

        log.info("Fetching details for story: {}", request.storyKey());

        String issueEndpoint = "/" + azureApiProperties.getProject() + "/_apis/wit/workitems/" + request.storyKey() + "?api-version=7.1";        

        String jsonResponse = webClient.get()
                .uri(issueEndpoint)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Log the full JSON response
        log.info("Full Azure Issue JSON Response: {}", jsonResponse);

        // Parse the JSON response to extract the acceptance criteria
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String description = extractAcceptanceCriteria(rootNode);

        // Create and return Response record
        return new Response(request.storyKey(), description);

    } catch (WebClientResponseException e) {
        log.error("Error response from Azure API: {}", e.getResponseBodyAsString(), e);
        throw new RuntimeException("Failed to fetch Azure issue details for story: " + request.storyKey(), e);
    } catch (Exception e) {
        log.error("Error fetching Azure issue details for story: {}", request.storyKey(), e);
        throw new RuntimeException("Failed to fetch Azure issue details for story: " + request.storyKey(), e);
    }
}


private String extractAcceptanceCriteria(JsonNode rootNode) {
    String acceptanceCriteria = "";

    try {
        // Navigate to the description field within the fields object
        JsonNode descriptionNode = rootNode.path("fields").path("System.Description");

        // Check if the description node exists and is not null
        if (descriptionNode.isMissingNode() || descriptionNode.isNull()) {
            log.warn("Description node is missing or null.");
        } else {
            // Extract the description directly as a string
            acceptanceCriteria = descriptionNode.asText();
        }
    } catch (Exception e) {
        log.error("Error extracting acceptance criteria", e);
    }

    log.info("Extracted Acceptance Criteria: {}", acceptanceCriteria.trim());
    return acceptanceCriteria.trim();
}


    

    public record Request(String storyKey) {}
    public record Response(String storyKey, String acceptanceCriteria) {}
}
 