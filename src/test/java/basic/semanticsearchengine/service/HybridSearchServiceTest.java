
package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HybridSearchServiceTest {

    @Mock
    private GenerateEmbeddings embeddingClient;

    @Mock
    private DocumentRepository repository;

    @Mock
    private DocumentReranker reranker;

    @Mock
    private ReciprocalRankFusion reciprocalRankFusion;

    @InjectMocks
    private SearchService searchService;

    private SearchResult result(String content) {
        return new SearchResult(
                new Document(content, new double[0]),
                0.5);
    }

    @Test
    void hybridSearchCombinesSemanticAndKeywordResults()
            throws Exception {

        double[] queryVector = {0.1, 0.2};

        List<SearchResult> semanticResults = List.of(
                result("Payment is pending"),
                result("Refund policy"));

        List<SearchResult> keywordResults = List.of(
                result("Payment is pending"));

        List<SearchResult> fusedResults = List.of(
                result("Payment is pending"),
                result("Refund policy"));

        when(embeddingClient.getEmbeddingResult("payment pending"))
                .thenReturn(queryVector);

        when(repository.search(queryVector, 2, 0.0))
                .thenReturn(semanticResults);

        when(repository.searchByKeyword("payment pending", 2))
                .thenReturn(keywordResults);

        // Stub fusion BEFORE calling hybridSearch().
        when(reciprocalRankFusion.fuse(
                semanticResults, keywordResults, 2))
                .thenReturn(fusedResults);

        List<SearchResult> results =
                searchService.hybridSearch("payment pending", 2);

        assertEquals(2, results.size());
        assertEquals(
                "Payment is pending",
                results.get(0).getDocument().getText());

        verify(embeddingClient)
                .getEmbeddingResult("payment pending");

        verify(repository)
                .search(queryVector, 2, 0.0);

        verify(repository)
                .searchByKeyword("payment pending", 2);

        verify(reciprocalRankFusion)
                .fuse(semanticResults, keywordResults, 2);
    }

    @Test
    void hybridSearchRejectsInvalidLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> searchService.hybridSearch("payment", 0));

        verifyNoInteractions(
                embeddingClient,
                repository,
                reciprocalRankFusion);
    }
}
