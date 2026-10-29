
package basic.semanticsearchengine.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OllamaChatClientIntegrationTest {

    @Autowired
    private OllamaChatClient ollamaChatClient;

    @Test
    void shouldGenerateRealResponseFromOllama() throws Exception {
        String answer = ollamaChatClient.generate(
                "Explain in one sentence why a payment might time out."
        );

        assertNotNull(answer);
        assertFalse(answer.isBlank());

        System.out.println("Real Ollama response: " + answer);

        assertTrue(
                answer.length() > 10,
                "Expected a meaningful response from Ollama"
        );
    }
}
