package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentIngestionIntegrationTest {

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private GenerateEmbeddings embeddingClient;

    @Autowired
    private DocumentRepository repository;

    @Test
    void ingestsDocumentAndFindsItUsingSemanticSearch()
            throws IOException, InterruptedException, SQLException {

        String documentText =
                "integration-payment-" + UUID.randomUUID()
                        + ": My bank card payment was declined.";

        try {
            // 1. Verify the real Ollama model produces a 768-dimensional vector.
            double[] embedding =
                    embeddingClient.getEmbeddingResult(documentText);

            assertNotNull(embedding);
            assertEquals(768, embedding.length);

            // 2. Ingest the document through the actual service.
            assertTrue(ingestionService.ingest(documentText));

            // 3. Verify it was persisted in PostgreSQL.
            assertTrue(repository.existsByContent(documentText));

            // 4. Re-ingesting the same text should not insert a duplicate.
            assertFalse(ingestionService.ingest(documentText));

            // 5. Generate a query embedding and search PostgreSQL.
            String query =
                    "Why was my credit card transaction rejected?";

            double[] queryEmbedding =
                    embeddingClient.getEmbeddingResult(query);

            List<SearchResult> results =
                    repository.search(queryEmbedding, 100, .50);

            // 6. Confirm the ingested document appears in search results.
            assertTrue(
                    results.stream().anyMatch(result ->
                            documentText.equals(
                                    result.getDocument().getText())),
                    "Semantic search should retrieve the ingested document"
            );

        } finally {
            // 7. Remove test data even if an assertion fails.
            repository.deleteByContent(documentText);
        }
    }

    @Test
    void ranksRelevantDocumentsAboveUnrelatedDocuments()
            throws IOException, InterruptedException, SQLException {

        String suffix = UUID.randomUUID().toString();

        String relevantText = "ranking-relevant-" + suffix
                + ": My debit card payment was declined by the bank.";

        String relatedText = "ranking-related-" + suffix
                + ": I cannot complete a purchase using my credit card.";

        String unrelatedText = "ranking-unrelated-" + suffix
                + ": The weather is sunny and birds are singing.";

        try {
            assertTrue(ingestionService.ingest(relevantText));
            assertTrue(ingestionService.ingest(relatedText));
            assertTrue(ingestionService.ingest(unrelatedText));

            String query = "Why was my bank card transaction rejected?";

            double[] queryEmbedding =
                    embeddingClient.getEmbeddingResult(query);

            List<SearchResult> results =
                    repository.search(queryEmbedding, 100, 0.0);

            SearchResult relevantResult = findResult(results, relevantText);
            SearchResult relatedResult = findResult(results, relatedText);
            SearchResult unrelatedResult = findResult(results, unrelatedText);

            assertTrue(
                    relevantResult.getScore() > unrelatedResult.getScore(),
                    "The relevant payment document should rank above the unrelated document"
            );

            assertTrue(
                    relatedResult.getScore() > unrelatedResult.getScore(),
                    "The related payment document should rank above the unrelated document"
            );

            System.out.println("Query: " + query);
            System.out.println("Relevant score: " + relevantResult.getScore());
            System.out.println("Related score: " + relatedResult.getScore());
            System.out.println("Unrelated score: " + unrelatedResult.getScore());

        } finally {
            repository.deleteByContent(relevantText);
            repository.deleteByContent(relatedText);
            repository.deleteByContent(unrelatedText);
        }
    }

    private SearchResult findResult(
            List<SearchResult> results,
            String content) {

        return results.stream()
                .filter(result ->
                        content.equals(result.getDocument().getText()))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Search result not found for: " + content));
    }
}