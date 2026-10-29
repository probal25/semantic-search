package basic.semanticsearchengine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OllamaChatClient {

    private final ChatHttpClient chatHttpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;

    public OllamaChatClient(
            ChatHttpClient chatHttpClient,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.chat-model}") String model) {

        this.chatHttpClient = chatHttpClient;
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public String generate(String prompt)
            throws IOException, InterruptedException {

        ObjectNode requestBody = objectMapper.createObjectNode();

        requestBody.put("model", model);
        requestBody.put("think", false);
        requestBody.put("stream", false);

        ArrayNode messages = requestBody.putArray("messages");

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);

        String url = baseUrl + "/api/chat";
        String jsonBody = objectMapper.writeValueAsString(requestBody);

        String responseBody = chatHttpClient.post(url, jsonBody);

        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode content = responseJson.path("message").path("content");

        if (!content.isTextual()) {
            throw new IOException(
                    "Invalid Ollama response: message.content is missing");
        }

        return content.asText();
    }
}
