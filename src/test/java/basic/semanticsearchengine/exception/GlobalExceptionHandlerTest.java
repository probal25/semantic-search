
package basic.semanticsearchengine.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new FailingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void embeddingFailureReturnsBadGateway()
            throws Exception {

        mockMvc.perform(get("/test/embedding-failure"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error")
                        .value("Embedding generation failed"))
                .andExpect(jsonPath("$.message")
                        .value("The document could not be processed"));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError()
            throws Exception {

        mockMvc.perform(get("/test/unexpected-failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal server error"))
                .andExpect(jsonPath("$.message")
                        .value("The request could not be completed"));
    }

    @Test
    void databaseFailureReturnsInternalServerError()
            throws Exception {

        mockMvc.perform(get("/test/database-failure"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Database operation failed"))
                .andExpect(jsonPath("$.message")
                        .value("The request could not be completed"));
    }

    @RestController
    static class FailingController {

        @GetMapping("/test/embedding-failure")
        public void fail() {
            throw new EmbeddingGenerationException(
                    "Simulated embedding failure",
                    new RuntimeException("Simulated provider failure")
            );
        }

        @GetMapping("/test/unexpected-failure")
        public void unexpectedFailure() {
            throw new IllegalStateException(
                    "Internal database connection details"
            );
        }

        @GetMapping("/test/database-failure")
        public void databaseFailure() {
            throw new DatabaseOperationException(
                    "Simulated database failure",
                    new RuntimeException("Simulated SQL failure")
            );
        }
    }
}
