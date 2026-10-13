package searchcontrollertest;

import basic.semanticsearchengine.controller.SearchController;
import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.domain.request.SearchRequest;
import basic.semanticsearchengine.domain.response.SearchResponse;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private GenerateEmbeddings embeddingClient;

    @Mock
    private DocumentRepository repository;

    @InjectMocks
    private SearchController controller;

    @Test
    void searchReturnsMatchingDocuments() throws Exception {
        // Arrange: define the fake embedding and database result
        double[] queryVector = {0.1, 0.2, 0.3};

        when(embeddingClient.getEmbeddingResult("payment failed"))
                .thenReturn(queryVector);

        Document document = new Document("My payment failed", new double[0]);
        SearchResult result = new SearchResult(document, 0.95);

        when(repository.search(queryVector, 5))
                .thenReturn(List.of(result));

        SearchRequest request = new SearchRequest();
        request.setQuery("payment failed");
        request.setLimit(5);

        // Act: execute the controller method
        List<SearchResponse> responses = controller.search(request);

        // Assert: check the response
        assertEquals(1, responses.size());
        assertEquals("My payment failed", responses.get(0).getText());
        assertEquals(0.95, responses.get(0).getScore(), 0.000001);

        verify(embeddingClient).getEmbeddingResult("payment failed");
        verify(repository).search(queryVector, 5);
    }

    @Test
    void searchPropagatesEmbeddingFailure() throws Exception {
        SearchRequest request = new SearchRequest();
        request.setQuery("payment failed");
        request.setLimit(5);

        when(embeddingClient.getEmbeddingResult("payment failed"))
                .thenThrow(new IOException("Ollama is unavailable"));

        IOException exception = assertThrows(
                IOException.class,
                () -> controller.search(request)
        );

        assertEquals("Ollama is unavailable", exception.getMessage());

        verify(repository, org.mockito.Mockito.never())
                .search(org.mockito.ArgumentMatchers.any(double[].class), org.mockito.ArgumentMatchers.anyInt());
    }
}