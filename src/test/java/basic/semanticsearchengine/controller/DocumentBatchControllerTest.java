package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.domain.response.BatchIngestionResponse;
import basic.semanticsearchengine.exception.BatchIngestionException;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.exception.GlobalExceptionHandler;
import basic.semanticsearchengine.service.DocumentIngestionService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@Import(GlobalExceptionHandler.class)
class DocumentBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentIngestionService ingestionService;

    @Test
    void validBatchReturns201AndCounts() throws Exception {
        when(ingestionService.ingestBatch(
                List.of(
                        "Card payment was declined",
                        "My bank transfer is delayed"
                )
        )).thenReturn(new BatchIngestionResponse(2, 2, 0));

        mockMvc.perform(post("/api/documents/batch")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "documents": [
                                    {"text": "Card payment was declined"},
                                    {"text": "My bank transfer is delayed"}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.created").value(2))
                .andExpect(jsonPath("$.duplicates").value(0));

        verify(ingestionService).ingestBatch(
                List.of(
                        "Card payment was declined",
                        "My bank transfer is delayed"
                )
        );
    }

    @Test
    void emptyBatchReturns400() throws Exception {
        mockMvc.perform(post("/api/documents/batch")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "documents": []
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ingestionService);
    }

    @Test
    void batchFailureReturns500WithPartialProgress() throws Exception {
        when(ingestionService.ingestBatch(
                List.of("Document A", "Document B", "Document C")
        )).thenThrow(new BatchIngestionException(
                "Batch ingestion failed at document index 1",
                3,
                1,
                0,
                1,
                new RuntimeException("Ollama unavailable")
        ));

        mockMvc.perform(post("/api/documents/batch")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "documents": [
                                {"text": "Document A"},
                                {"text": "Document B"},
                                {"text": "Document C"}
                              ]
                            }
                            """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Batch ingestion failed"))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.duplicates").value(0))
                .andExpect(jsonPath("$.failedIndex").value(1));
    }


    @Test
    void batchEmbeddingFailureReturns502WithPartialProgress() throws Exception {
        when(ingestionService.ingestBatch(
                List.of("Document A", "Document B")
        )).thenThrow(new BatchIngestionException(
                "Batch ingestion failed at document index 1",
                2,
                1,
                0,
                1,
                new EmbeddingGenerationException(
                        "Failed to generate document embedding",
                        new IOException("Ollama unavailable")
                )
        ));

        mockMvc.perform(post("/api/documents/batch")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "documents": [
                                {"text": "Document A"},
                                {"text": "Document B"}
                              ]
                            }
                            """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.error")
                        .value("Batch embedding generation failed"))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.created").value(1))
                .andExpect(jsonPath("$.duplicates").value(0))
                .andExpect(jsonPath("$.failedIndex").value(1));
    }


    @Test
    void batchExceeding100DocumentsReturns400() throws Exception {
        StringBuilder documents = new StringBuilder("[");
        for (int i = 0; i < 101; i++) {
            if (i > 0) {
                documents.append(",");
            }
            documents.append("{\"text\":\"Document ")
                    .append(i)
                    .append("\"}");
        }
        documents.append("]");

        mockMvc.perform(post("/api/documents/batch")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {"documents": %s}
                            """.formatted(documents)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ingestionService);
    }
}
