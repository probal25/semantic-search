package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.exception.GlobalExceptionHandler;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;

import java.util.List;

import static org.mockito.Mockito.when;



@WebMvcTest(SearchController.class)
@Import(GlobalExceptionHandler.class)
class SearchControllerHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenerateEmbeddings embeddingClient;

    @MockBean
    private DocumentRepository repository;

    @Test
    void blankQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "   ",
                                  "limit": 5
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.query")
                        .value("Query must not be blank"));
    }

    @Test
    void validQueryReturnsSearchResults() throws Exception {
        double[] queryVector = {0.1, 0.2, 0.3};

        when(embeddingClient.getEmbeddingResult("payment failed"))
                .thenReturn(queryVector);

        Document document =
                new Document("My payment failed", new double[0]);

        SearchResult result = new SearchResult(document, 0.95);

        when(repository.search(queryVector, 5))
                .thenReturn(List.of(result));

        mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "query": "payment failed",
                              "limit": 5
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text")
                        .value("My payment failed"))
                .andExpect(jsonPath("$[0].score")
                        .value(0.95));

        verify(embeddingClient)
                .getEmbeddingResult("payment failed");

        verify(repository).search(queryVector, 5);
    }
}