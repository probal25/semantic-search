package basic.semanticsearchengine.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearchMetricsTest {

    private final SearchMetrics metrics = new SearchMetrics();

    @Test
    void calculatesPrecisionAtK() {
        List<String> retrieved = List.of("D1", "D3", "D2", "D4");
        Set<String> relevant = Set.of("D1", "D2");

        assertEquals(
                2.0 / 3.0,
                metrics.precisionAtK(retrieved, relevant, 3),
                0.0001
        );
    }

    @Test
    void calculatesRecallAtK() {
        List<String> retrieved = List.of("D1", "D3", "D2");
        Set<String> relevant = Set.of("D1", "D2", "D5", "D6");

        assertEquals(
                0.5,
                metrics.recallAtK(retrieved, relevant, 3),
                0.0001
        );
    }

    @Test
    void handlesFewerResultsThanK() {
        List<String> retrieved = List.of("D1", "D3");
        Set<String> relevant = Set.of("D1", "D2");

        assertEquals(
                0.2,
                metrics.precisionAtK(retrieved, relevant, 5),
                0.0001
        );

        assertEquals(
                0.5,
                metrics.recallAtK(retrieved, relevant, 5),
                0.0001
        );
    }

    @Test
    void rejectsNonPositiveK() {
        assertThrows(
                IllegalArgumentException.class,
                () -> metrics.precisionAtK(
                        List.of("D1"),
                        Set.of("D1"),
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> metrics.recallAtK(
                        List.of("D1"),
                        Set.of("D1"),
                        -1
                )
        );
    }

    @Test
    void rejectsEmptyRelevantSetForRecall() {
        assertThrows(
                IllegalArgumentException.class,
                () -> metrics.recallAtK(
                        List.of("D1"),
                        Set.of(),
                        1
                )
        );
    }

    @Test
    void returnsZeroPrecisionWhenNoDocumentsAreRetrieved() {
        List<String> retrieved = List.of();
        Set<String> relevant = Set.of("D1", "D2");

        assertEquals(
                0.0,
                metrics.precisionAtK(retrieved, relevant, 5),
                0.0001
        );
    }

    @Test
    void calculatesPrecisionAmongReturnedDocuments() {
        List<String> retrievedIds = List.of("D1", "D2");
        Set<String> relevantIds = Set.of("D1", "D2");

        double precision = SearchMetrics.precisionAmongReturned(
                retrievedIds, relevantIds);

        assertEquals(1.0, precision);
    }

    @Test
    void calculatesPrecisionWhenSomeReturnedDocumentsAreIrrelevant() {
        List<String> retrievedIds = List.of("D1", "D3");
        Set<String> relevantIds = Set.of("D1", "D2");

        double precision = SearchMetrics.precisionAmongReturned(
                retrievedIds, relevantIds);

        assertEquals(0.5, precision);
    }

    @Test
    void returnsZeroWhenNoDocumentsAreReturned() {
        double precision = SearchMetrics.precisionAmongReturned(
                List.of(), Set.of("D1"));

        assertEquals(0.0, precision);
    }

    @Test
    void calculatesPrecisionAtOneWhenTopResultIsRelevant() {
        List<String> retrievedIds = List.of("D1", "D3", "D2");
        Set<String> relevantIds = Set.of("D1", "D2");

        assertEquals(
                1.0,
                metrics.precisionAtK(retrievedIds, relevantIds, 1)
        );
    }

    @Test
    void calculatesPrecisionAtOneWhenTopResultIsIrrelevant() {
        List<String> retrievedIds = List.of("D3", "D1", "D2");
        Set<String> relevantIds = Set.of("D1", "D2");

        assertEquals(
                0.0,
                metrics.precisionAtK(retrievedIds, relevantIds, 1)
        );
    }
}
