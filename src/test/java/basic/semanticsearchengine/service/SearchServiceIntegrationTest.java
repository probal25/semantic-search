
package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchCandidates;
import basic.semanticsearchengine.domain.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchServiceIntegrationTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private MmrDocumentReranker mmrDocumentReranker;

    @Test
    void retrieveCandidatesReturnsEmbeddingAndResults()
            throws IOException, InterruptedException, SQLException {

        String query =
                "Why might a payment get stuck or time out?";

        int candidateCount = 10;

        SearchCandidates candidates =
                searchService.retrieveCandidates(
                        query, candidateCount);

        assertNotNull(candidates);
        assertNotNull(candidates.queryEmbedding());
        assertTrue(candidates.queryEmbedding().length > 0);

        assertNotNull(candidates.results());
        assertTrue(
                candidates.results().size() <= candidateCount);

        System.out.println("Query embedding dimensions: "
                + candidates.queryEmbedding().length);

        System.out.println("Retrieved candidates: "
                + candidates.results().size());

        candidates.results().forEach(result ->
                System.out.println(
                        "RRF score: " + result.getScore()
                                + " | Text: "
                                + result.getDocument().getText()));
    }

    @Test
    void mmrReranksRealSearchCandidates()
            throws IOException, InterruptedException, SQLException {

        String query = "Why might a payment get stuck or time out?";

        SearchCandidates candidates =
                searchService.retrieveCandidates(query, 10);

        System.out.println(
                "Query dimensions: " + candidates.queryEmbedding().length);

        candidates.results().forEach(result ->
                System.out.println(
                        "Document: " + result.getDocument().getText()
                                + " | Embedding dimensions: "
                                + (result.getDocument().getEmbedding() == null
                                ? "null"
                                : result.getDocument().getEmbedding().length)));

        List<SearchResult> selected =
                mmrDocumentReranker.rerank(
                        candidates.queryEmbedding(),
                        candidates.results(),
                        3,
                        0.7);

        System.out.println("\nOriginal RRF top 3:");

        candidates.results().stream()
                .limit(3)
                .forEach(result -> System.out.println(
                        "RRF: " + result.getScore()
                                + " | Text: " + result.getDocument().getText()
                ));

        System.out.println("\nMMR-selected top 3:");

        selected.forEach(result -> System.out.println(
                "RRF: " + result.getScore()
                        + " | MMR: " + result.getRerankingScore()
                        + " | Text: " + result.getDocument().getText()
        ));

        assertEquals(
                Math.min(3, candidates.results().size()),
                selected.size());

        System.out.println("\nMMR-selected documents:");

        selected.forEach(result ->
                System.out.println(
                        "RRF: " + result.getScore()
                                + " | MMR: " + result.getRerankingScore()
                                + " | Text: "
                                + result.getDocument().getText()));
    }
}
