# Azure Stories Test Case Generation Application with Ollama Model Integration

## Overview

The Azure Stories Test Case Generation Application is a Spring Boot application designed to fetch user story details from Azure and generate test cases based on the acceptance criteria. It integrates with the Ollama AI model to process natural language inputs and automatically generate detailed test cases.

## Project Structure

- **AzureInquiryController**:
  - Handles HTTP requests for Azure story inquiries.
  - Utilizes the Ollama AI model to process acceptance criteria and generate relevant test cases.

- **AzureFunctionConfig**:
  - Configures and provides the `AzureDataServiceGetUs` and `AzureDataServiceCreateTc` beans.
  - Sets up the Azure function used by the AI model to fetch Azure story details and generate test cases.

- **AzureDataServiceGetUS**:
  - Manages communication with the Azure API to fetch user story details, including acceptance criteria.
  - Reads Azure API credentials and URL from configuration properties.
  - Parses the Azure API responses to extract relevant information.

  - **AzureDataServiceCreateTc**:
  - Sends Ollama AI model generated test cases to Azure to create the test cases on Azure plattform
  - Reads Azure API credentials and URL from configuration properties.
  - Parses the Azure API responses to extract relevant information.


- **AzureApiProperties**:
  - Stores Azure API credentials (username, API token) and the base URL.
  - Provides configuration properties for `AzureDataServiceGetUs` and `AzureDataServiceCreateTc`.

- **PromptProperties**:
  - Stores the prompts to be sent to the Ollama AI model
  - Provides configuration properties for `PromptDataServiceGetProp`.

## Configuration

1. **Set up your environment**:
   - Ensure you have Java 17 and Gradle installed.
   - Install Ollama
   - Enable a ollama model like mistal:  
   - command: ollama runs mistral   


2. **Configure the application properties**:
   - Update `src/main/resources/application.properties` with your Azure API credentials:
     ```properties
     azure.apiToken=YOUR_AZURE_API_TOKEN
     azure.apiUrl=https://dev.azure.com/YOUR_DEVOPS_ORG_NAME
     azure.project=YOUR_AZURE_DEVOPS_PROJECT
     ```
  - Update `src/main/resources/application.properties` with the ollama model to be used. Ex: mistral:
     ```properties
     spring.application.name=ollamarestapi
     spring.ai.ollama.chat.model=mistral
    ```

## Running the Application

1. **Build and Start the Application**:
   ```sh
   ./gradlew clean build bootRun


2. **Send Request to the Application from Postman/Httpie or any other tool, to geet the test cases as a response of the Ollama AI model based on the description of an Azure User Story**:
   ```bash
   GET http://localhost:8080/api/v1/azure-story-testcases?storyKey=1

3. **Send Request to the Application from Postman/Httpie or any other tool, to get the user story problemas as a response of the Ollama AI model based on the description of an Azure User Story**:
   ```bash   
   GET http://localhost:8080/api/v1/azure-story-problems?storyKey=1

4. **Send Request to the Application from Postman/Httpie or any other tool, to create the test cases on Auzre based on a response of the Ollama AI model of the description of an Azure User Story**:
   ```bash
   GET http://localhost:8080/api/v1/azure-create-testcases?storyKey=1

5. **Send Request to the Application from Postman/Httpie or any other tool, to create the test cases on Auzre based on sending on the body of the call, a json with the test cases (that could be generated with point 2 endpoint call)**:
   ```bash   
   http://localhost:8080/api/v1/azure-create-testcases-bobyAIResponse?storyKey=1



