package basic.semanticsearchengine.evaluation;

import java.util.List;
import java.util.Set;

public class SearchMetrics {

    public double precisionAtK(
            List<String> retrievedDocumentIds,
            Set<String> relevantDocumentIds,
            int k) {

        validateK(k);

        int relevantRetrieved = countRelevantRetrieved(
                retrievedDocumentIds,
                relevantDocumentIds,
                k
        );

        return (double) relevantRetrieved / k;
    }

    public double recallAtK(
            List<String> retrievedDocumentIds,
            Set<String> relevantDocumentIds,
            int k) {

        validateK(k);

        if (relevantDocumentIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Relevant document set must not be empty"
            );
        }

        int relevantRetrieved = countRelevantRetrieved(
                retrievedDocumentIds,
                relevantDocumentIds,
                k
        );

        return (double) relevantRetrieved / relevantDocumentIds.size();
    }

    public static double precisionAmongReturned(List<String> retrievedIds, Set<String> relevantIds) {

        if (retrievedIds.isEmpty()) {
            return 0.0;
        }

        long relevantRetrieved = retrievedIds.stream()
                .filter(relevantIds::contains)
                .count();

        return (double) relevantRetrieved / retrievedIds.size();
    }

    public double reciprocalRank(
            List<String> retrievedDocumentIds,
            Set<String> relevantDocumentIds) {

        for (int i = 0; i < retrievedDocumentIds.size(); i++) {
            if (relevantDocumentIds.contains(retrievedDocumentIds.get(i))) {
                return 1.0 / (i + 1);
            }
        }

        return 0.0;
    }

    private int countRelevantRetrieved(
            List<String> retrievedDocumentIds,
            Set<String> relevantDocumentIds,
            int k) {

        int relevantRetrieved = 0;
        int resultsToEvaluate = Math.min(k, retrievedDocumentIds.size());

        for (int i = 0; i < resultsToEvaluate; i++) {
            if (relevantDocumentIds.contains(retrievedDocumentIds.get(i))) {
                relevantRetrieved++;
            }
        }

        return relevantRetrieved;
    }

    private void validateK(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException(
                    "k must be greater than 0"
            );
        }
    }

}