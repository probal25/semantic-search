package searchcontrollertest;
import basic.semanticsearchengine.controller.SearchController;
import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.domain.request.SearchRequest;
import basic.semanticsearchengine.domain.response.SearchResponse;
import basic.semanticsearchengine.service.SearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController controller;

    @Test
    void searchReturnsMatchingDocuments() throws Exception {
        // Arrange
        Document document = new Document(
                "My payment failed",
                new double[0]
        );

        SearchResult result = new SearchResult(document, 0.95);

        when(searchService.search("payment failed", 5))
                .thenReturn(List.of(result));

        SearchRequest request = new SearchRequest();
        request.setQuery("payment failed");
        request.setLimit(5);

        // Act
        List<SearchResponse> responses = controller.search(request);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("My payment failed", responses.get(0).getText());
        assertEquals(0.95, responses.get(0).getScore(), 0.000001);

        verify(searchService).search("payment failed", 5);
    }

    @Test
    void searchPropagatesEmbeddingFailure() throws Exception {
        // Arrange
        SearchRequest request = new SearchRequest();
        request.setQuery("payment failed");
        request.setLimit(5);

        when(searchService.search("payment failed", 5))
                .thenThrow(new IOException("Ollama is unavailable"));

        // Act
        IOException exception = assertThrows(
                IOException.class,
                () -> controller.search(request)
        );

        // Assert
        assertEquals("Ollama is unavailable", exception.getMessage());
        verify(searchService).search("payment failed", 5);
    }

}