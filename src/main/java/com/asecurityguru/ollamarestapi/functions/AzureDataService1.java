package com.asecurityguru.ollamarestapi.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.function.Function;

@Validated
public class AzureDataService1 implements Function<AzureDataService1.Request, AzureDataService1.Response> {

    private static final Logger log = LoggerFactory.getLogger(AzureDataService.class);
    private final AzureApiProperties azureApiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient;

    public AzureDataService1(AzureApiProperties azureProps) {
        this.azureApiProperties = azureProps;
        this.webClient = WebClient.builder()
                .baseUrl(azureApiProperties.getApiUrl())
                .defaultHeaders(headers -> headers.setBearerAuth(azureApiProperties.getApiToken()))
                .build();
    }

@Override
public Response apply(Request request) {

    try {
        log.info("Creating test case with title: {}, steps: {} and expected result: {}", request.title(), request.steps());

        String createTestCaseEndpoint = "/" + azureApiProperties.getProject() + "/_apis/wit/workitems/$Test Case?api-version=7.1";

        // Construct the JSON payload for the test case
        String jsonPayload = String.format(
            "[{\"op\": \"add\", \"path\": \"/fields/System.Title\", \"value\": \"%s\"}," +
            "{\"op\": \"add\", \"path\": \"/fields/Microsoft.VSTS.TCM.Steps\", \"value\": \"%s\"}]",
            request.title(), request.steps()
        );

        log.info("Azure Create Test Case JSON Payload: {}", jsonPayload);

        String jsonResponse = webClient.post()
                .uri(createTestCaseEndpoint)
                .header("Content-Type", "application/json-patch+json")
                .bodyValue(jsonPayload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Log the full JSON response
        log.info("Full Azure Create Test Case JSON Response: {}", jsonResponse);

        // Parse the JSON response to extract the test case ID or other relevant information
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String testCaseId = rootNode.path("id").asText();

        // Create and return Response record
        return new Response(testCaseId, "Test case created successfully");

    } catch (WebClientResponseException e) {
        log.error("Error response from Azure API: {}", e.getResponseBodyAsString(), e);
        throw new RuntimeException("Failed to create Azure test case", e);
    } catch (Exception e) {
        log.error("Error creating Azure test case", e);
        throw new RuntimeException("Failed to create Azure test case", e);
    }
}


    public record Request(String title, String steps) {}
    public record Response(String id, String message) {}
}
 