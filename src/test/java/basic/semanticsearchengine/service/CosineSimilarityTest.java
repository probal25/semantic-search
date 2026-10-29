
package basic.semanticsearchengine.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CosineSimilarityTest {

    private final CosineSimilarity cosineSimilarity =
            new CosineSimilarity();

    @Test
    void returnsOneForVectorsPointingInSameDirection() {
        double result = cosineSimilarity.calculate(
                new double[]{1, 2},
                new double[]{2, 4});

        assertEquals(1.0, result, 1e-10);
    }

    @Test
    void returnsZeroForPerpendicularVectors() {
        double result = cosineSimilarity.calculate(
                new double[]{1, 0},
                new double[]{0, 1});

        assertEquals(0.0, result, 1e-10);
    }

    @Test
    void returnsMinusOneForOppositeVectors() {
        double result = cosineSimilarity.calculate(
                new double[]{1, 0},
                new double[]{-1, 0});

        assertEquals(-1.0, result, 1e-10);
    }

    @Test
    void rejectsNullVectors() {
        assertThrows(IllegalArgumentException.class,
                () -> cosineSimilarity.calculate(null,
                        new double[]{1, 2}));
    }

    @Test
    void rejectsDifferentDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> cosineSimilarity.calculate(
                        new double[]{1, 2},
                        new double[]{1, 2, 3}));
    }

    @Test
    void rejectsZeroMagnitudeVectors() {
        assertThrows(IllegalArgumentException.class,
                () -> cosineSimilarity.calculate(
                        new double[]{0, 0},
                        new double[]{1, 2}));
    }
}
