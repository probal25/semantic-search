
package basic.semanticsearchengine.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaChatClientTest {

    @Test
    void generateReturnsAssistantContent() throws Exception {
        // Arrange: simulate Ollama's HTTP response
        ChatHttpClient fakeHttpClient = (url, jsonBody) -> """
                {
                    "message": {
                        "role": "assistant",
                        "content": "Your payment may have timed out."
                    },
                    "done": true
                }
                """;

        OllamaChatClient chatClient = new OllamaChatClient(
                fakeHttpClient,
                "http://localhost:11434",
                "qwen3.5:4b"
        );

        // Act
        String answer = chatClient.generate(
                "Why did my payment time out?");

        // Assert
        assertEquals(
                "Your payment may have timed out.",
                answer
        );
    }

    @Test
    void generateSendsRequestToOllamaChatEndpoint() throws Exception {
        String[] capturedUrl = new String[1];

        ChatHttpClient fakeHttpClient = (url, jsonBody) -> {
            capturedUrl[0] = url;

            return """
                    {
                        "message": {
                            "role": "assistant",
                            "content": "Test answer"
                        },
                        "done": true
                    }
                    """;
        };

        OllamaChatClient chatClient = new OllamaChatClient(
                fakeHttpClient,
                "http://localhost:11434",
                "qwen3.5:4b"
        );

        chatClient.generate("Test prompt");

        assertEquals(
                "http://localhost:11434/api/chat",
                capturedUrl[0]
        );
    }

    @Test
    void generateThrowsIOExceptionWhenResponseHasNoContent() {
        ChatHttpClient fakeHttpClient = (url, jsonBody) -> """
                {
                    "message": {
                        "role": "assistant"
                    },
                    "done": true
                }
                """;

        OllamaChatClient chatClient = new OllamaChatClient(
                fakeHttpClient,
                "http://localhost:11434",
                "qwen3.5:4b"
        );

        IOException exception = org.junit.jupiter.api.Assertions
                .assertThrows(
                        IOException.class,
                        () -> chatClient.generate("Test prompt")
                );

        assertTrue(exception.getMessage().contains("message.content"));
    }
}
