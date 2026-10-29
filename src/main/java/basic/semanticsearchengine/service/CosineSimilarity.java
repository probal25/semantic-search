
package basic.semanticsearchengine.service;

import org.springframework.stereotype.Component;

@Component
public class CosineSimilarity {

    public double calculate(double[] a, double[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException(
                    "Vectors must not be null");
        }

        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimensions");
        }

        if (a.length == 0) {
            throw new IllegalArgumentException(
                    "Vectors must not be empty");
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            magnitudeA += a[i] * a[i];
            magnitudeB += b[i] * b[i];
        }

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            throw new IllegalArgumentException(
                    "Vectors must have non-zero magnitude");
        }

        return dotProduct
                / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }
}
