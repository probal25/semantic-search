package basic.semanticsearchengine.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationDatasetLoaderTest {

    private final EvaluationDatasetLoader loader =
            new EvaluationDatasetLoader(new ObjectMapper());

    @Test
    void loadsSearchCases() throws IOException {
        List<SearchCase> cases = loader.loadSearchCases();

        assertEquals(8, cases.size());
        assertEquals(
                "Why did my bank card payment fail?",
                cases.get(0).query()
        );
        assertEquals(
                List.of("D1", "D2", "D4", "D5", "D6"),
                cases.get(0).relevantDocumentIds()
        );
    }

    @Test
    void loadsEvaluationDocuments() throws IOException {
        List<EvaluationDocument> documents = loader.loadDocuments();

        assertEquals(12, documents.size());
        assertEquals("D1", documents.get(0).id());
        assertEquals(
                "My debit card payment was declined by the bank.",
                documents.get(0).content()
        );
    }
}