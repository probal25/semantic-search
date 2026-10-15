package basic.semanticsearchengine.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationDocumentIndexTest {

    private final EvaluationDocumentIndex index =
            new EvaluationDocumentIndex(List.of(
                    new EvaluationDocument(
                            "D1",
                            "My debit card payment was declined by the bank."
                    ),
                    new EvaluationDocument(
                            "D2",
                            "I cannot complete a purchase using my credit card."
                    ),
                    new EvaluationDocument(
                            "D3",
                            "The weather is sunny and birds are singing."
                    )
            ));

    @Test
    void findsEvaluationIdForKnownContent() {
        assertEquals(
                "D1",
                index.findIdByContent(
                        "My debit card payment was declined by the bank."
                )
        );
    }

    @Test
    void returnsNullForUnknownContent() {
        assertNull(index.findIdByContent("A document outside the test dataset"));
    }
}