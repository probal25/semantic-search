package basic.semanticsearchengine.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class OllamaGenerateEmbeddings implements GenerateEmbeddings {

    @Value("${ollama.base-url}")
    private String baseUrl;

    @Value("${ollama.embedding-model}")
    private String model;

    private static final String OLLAMA_EMBEDDED_PATH =
            "/api/embed";

    private static final HttpClient HTTP_CLIENT =
            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

    @Override
    public double[] getEmbeddingResult(String text) throws IOException, InterruptedException {

        ObjectMapper mapper = new ObjectMapper();

        ObjectNode requestJson = mapper.createObjectNode();
        requestJson.put("model", model);
        requestJson.putArray("input").add(text);

        String requestBody = mapper.writeValueAsString(requestJson);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + OLLAMA_EMBEDDED_PATH))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(
                    "Ollama returned HTTP " + response.statusCode()
                            + ": " + response.body()
            );
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode embeddings = root.path("embeddings");

        return mapper.treeToValue(
                embeddings.path(0),
                double[].class
        );
    }
}
