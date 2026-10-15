package basic.semanticsearchengine.repository;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentRepositoryIntegrationTest {

    @Autowired
    private DocumentRepository repository;

    private String testContent;

    @AfterEach
    void cleanUp() {
        if (testContent != null) {
            repository.deleteByContent(testContent);
        }
    }

    @Test
    void savesDocumentAndDetectsExistingContent() {
        testContent = "integration-test-" + UUID.randomUUID();

        double[] embedding = new double[768];
        Arrays.fill(embedding, 0.01);

        Document document = new Document(testContent, embedding);

        assertTrue(repository.save(document),
                "A new document should be inserted");

        assertTrue(repository.existsByContent(testContent),
                "The repository should find the inserted document");

        assertFalse(repository.save(document),
                "A duplicate should not be inserted");
    }

    @Test
    void existsByContentReturnsFalseForMissingDocument() {
        String missingContent =
                "integration-missing-" + UUID.randomUUID();

        assertFalse(repository.existsByContent(missingContent),
                "A document that does not exist should return false");
    }

    @Test
    void searchRanksMostSimilarDocumentFirst() {
        String exactMatch =
                "integration-exact-" + UUID.randomUUID();
        String similarContent =
                "integration-similar-" + UUID.randomUUID();
        String differentDocument =
                "integration-different-" + UUID.randomUUID();

        double[] queryVector = new double[768];
        queryVector[0] = 1.0;

        double[] similarVector = new double[768];
        similarVector[0] = 1.0;
        similarVector[1] = 0.01;

        double[] differentVector = new double[768];
        differentVector[1] = 1.0;

        try {
            assertTrue(repository.save(
                    new Document(exactMatch, queryVector)));

            assertTrue(repository.save(
                    new Document(similarContent, similarVector)));

            assertTrue(repository.save(
                    new Document(differentDocument, differentVector)));

            // Search beyond the two expected test documents because
            // the development database already contains other records.
            List<SearchResult> results =
                    repository.search(queryVector, 100, 0.0);

            SearchResult exactResult = findResult(results, exactMatch);
            SearchResult similarResult = findResult(results, similarContent);
            SearchResult differentResult =
                    findResult(results, differentDocument);

            assertEquals(1.0, exactResult.getScore(), 0.0001,
                    "An identical vector should have cosine similarity 1");

            assertTrue(
                    exactResult.getScore() > similarResult.getScore(),
                    "The exact match should rank above the similar vector");

            assertTrue(
                    similarResult.getScore() > differentResult.getScore(),
                    "The similar vector should rank above the orthogonal vector");

            // Verify the returned order for the three test documents.
            int exactIndex = indexOf(results, exactMatch);
            int similarIndex = indexOf(results, similarContent);
            int differentIndex = indexOf(results, differentDocument);

            assertTrue(exactIndex < similarIndex);
            assertTrue(similarIndex < differentIndex);

        } finally {
            repository.deleteByContent(exactMatch);
            repository.deleteByContent(similarContent);
            repository.deleteByContent(differentDocument);
        }
    }

    @Test
    void findAllIncludesInsertedDocument() {
        testContent = "integration-findall-" + UUID.randomUUID();

        double[] embedding = new double[768];
        embedding[0] = 1.0;

        assertTrue(repository.save(new Document(testContent, embedding)));

        List<Document> documents = repository.findAll();

        assertTrue(
                documents.stream().anyMatch(
                        document -> testContent.equals(document.getText())),
                "findAll should include the inserted document");
    }

    @Test
    void deleteByContentRemovesDocument() {
        String content = "integration-delete-" + UUID.randomUUID();

        double[] embedding = new double[768];
        embedding[0] = 1.0;

        try {
            assertTrue(repository.save(new Document(content, embedding)));
            assertTrue(repository.existsByContent(content));

            repository.deleteByContent(content);

            assertFalse(repository.existsByContent(content),
                    "The document should no longer exist after deletion");
        } finally {
            repository.deleteByContent(content);
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

    private int indexOf(
            List<SearchResult> results,
            String content) {

        for (int i = 0; i < results.size(); i++) {
            if (content.equals(results.get(i).getDocument().getText())) {
                return i;
            }
        }

        return -1;
    }
}
