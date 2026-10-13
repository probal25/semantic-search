package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.domain.request.DocumentIngestionRequest;
import basic.semanticsearchengine.exception.DatabaseOperationException;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.exception.GlobalExceptionHandler;
import basic.semanticsearchengine.service.DocumentIngestionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import(GlobalExceptionHandler.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentIngestionService ingestionService;

    @Test
    void validDocumentReturns201() throws Exception {
        String text = "My bank transfer is stuck";

        when(ingestionService.ingest(text)).thenReturn(true);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "My bank transfer is stuck"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Document ingested successfully"));

        verify(ingestionService).ingest(text);
    }

    @Test
    void blankDocumentReturns400() throws Exception {
        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": " "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ingestionService);
    }

    @Test
    void embeddingFailureReturns502() throws Exception {
        String text = "My bank transfer is stuck";

        doThrow(new EmbeddingGenerationException(
                "Failed to generate document embedding",
                new IOException("Ollama unavailable")
        )).when(ingestionService).ingest(text);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "My bank transfer is stuck"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error")
                        .value("Embedding generation failed"))
                .andExpect(jsonPath("$.message")
                        .value("The document could not be processed"));
    }

    @Test
    void unexpectedFailureReturns500() throws Exception {
        String text = "My bank transfer is stuck";

        doThrow(new RuntimeException("Database unavailable"))
                .when(ingestionService).ingest(text);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "My bank transfer is stuck"
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal server error"));
    }

    @Test
    void duplicateDocumentReturns200() throws Exception {
        String text = "My bank transfer is stuck";

        when(ingestionService.ingest(text)).thenReturn(false);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "My bank transfer is stuck"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Document already exists"));
    }

    @Test
    void databaseFailureReturnsSafeInternalServerError()
            throws Exception {

        String text = "My bank transfer is stuck";

        when(ingestionService.ingest(text))
                .thenThrow(new DatabaseOperationException(
                        "Database connection failed",
                        new RuntimeException("Sensitive internal details")
                ));

        DocumentIngestionRequest request =
                new DocumentIngestionRequest();

        request.setText(text);

        mockMvc.perform(post("/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Database operation failed"))
                .andExpect(jsonPath("$.message")
                        .value("The request could not be completed"))
                .andExpect(content().string(
                        not(containsString("Sensitive internal details"))
                ));
    }
}
