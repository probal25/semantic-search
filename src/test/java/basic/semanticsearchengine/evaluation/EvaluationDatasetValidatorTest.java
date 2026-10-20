package basic.semanticsearchengine.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationDatasetValidatorTest {

    private final EvaluationDatasetValidator validator =
            new EvaluationDatasetValidator();

    @Test
    void acceptsValidDataset() {
        List<EvaluationDocument> documents = List.of(
                new EvaluationDocument("D1", "My payment failed"),
                new EvaluationDocument("D2", "The weather is sunny")
        );

        List<SearchCase> searchCases = List.of(
                new SearchCase(
                        "Why did my payment fail?",
                        List.of("D1"),
                        Map.of("D1", 3)
                )
        );

        assertDoesNotThrow(
                () -> validator.validate(documents, searchCases)
        );
    }

    @Test
    void rejectsDuplicateDocumentIds() {
        List<EvaluationDocument> documents = List.of(
                new EvaluationDocument("D1", "My payment failed"),
                new EvaluationDocument("D1", "The weather is sunny")
        );

        List<SearchCase> searchCases = List.of();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(documents, searchCases)
        );

        assertTrue(exception.getMessage().contains(
                "Duplicate evaluation document ID: D1"));
    }

    @Test
    void rejectsDuplicateDocumentContent() {
        List<EvaluationDocument> documents = List.of(
                new EvaluationDocument("D1", "My payment failed"),
                new EvaluationDocument("D2", "My payment failed")
        );

        List<SearchCase> searchCases = List.of();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(documents, searchCases)
        );

        assertTrue(exception.getMessage().contains(
                "Duplicate evaluation document content"));
    }

    @Test
    void rejectsUnknownRelevantDocumentIds() {
        List<EvaluationDocument> documents = List.of(
                new EvaluationDocument("D1", "My payment failed")
        );

        List<SearchCase> searchCases = List.of(
                new SearchCase(
                        "Why did my payment fail?",
                        List.of("D99"),
                        Map.of()
                )
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(documents, searchCases)
        );

        assertTrue(exception.getMessage().contains(
                "Search case references unknown document ID: D99"));
    }
}