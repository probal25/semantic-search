package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexicalDocumentRerankerTest {

    private final LexicalDocumentReranker reranker =
            new LexicalDocumentReranker();

    @Test
    void ranksDocumentsByLexicalOverlapAndPreservesCosineScore() {
        List<SearchResult> candidates = List.of(
                new SearchResult(
                        new Document("The weather is sunny today", new double[0]),
                        0.90
                ),
                new SearchResult(
                        new Document("My payment failed", new double[0]),
                        0.70
                )
        );

        List<SearchResult> results = reranker.rerank(
                "My payment failed",
                candidates
        );

        assertEquals(
                "My payment failed",
                results.get(0).getDocument().getText()
        );

        assertEquals(1.0, results.get(0).getRerankingScore(), 0.0001);
        assertEquals(0.70, results.get(0).getScore(), 0.0001);

        assertEquals(
                "The weather is sunny today",
                results.get(1).getDocument().getText()
        );

        assertEquals(0.0, results.get(1).getRerankingScore(), 0.0001);
        assertEquals(0.90, results.get(1).getScore(), 0.0001);
    }
}