package basic.semanticsearchengine.evaluation;

import java.util.*;

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

    public double ndcgAtK(
            List<String> retrievedDocumentIds,
            Map<String, Integer> relevanceGrades,
            int k) {

        validateK(k);

        if (relevanceGrades.isEmpty()) {
            throw new IllegalArgumentException(
                    "Relevance grades must not be empty"
            );
        }

        Set<String> seen = new HashSet<>();
        List<Integer> retrievedGrades = new ArrayList<>();

        for (String documentId : retrievedDocumentIds.stream().limit(k).toList()) {
            if (seen.add(documentId)) {
                retrievedGrades.add(relevanceGrades.getOrDefault(documentId, 0));
            } else {
                retrievedGrades.add(0);
            }
        }

        double dcg = calculateDcg(retrievedGrades);

        double idealDcg = calculateDcg(
                relevanceGrades.values().stream()
                        .sorted(java.util.Comparator.reverseOrder())
                        .limit(k)
                        .toList()
        );

        if (idealDcg == 0.0) {
            return 0.0;
        }

        return dcg / idealDcg;
    }


    private int countRelevantRetrieved(
            List<String> retrievedDocumentIds,
            Set<String> relevantDocumentIds,
            int k) {

        int relevantRetrieved = 0;
        Set<String> seen = new HashSet<>();
        int resultsToEvaluate = Math.min(k, retrievedDocumentIds.size());

        for (int i = 0; i < resultsToEvaluate; i++) {
            String documentId = retrievedDocumentIds.get(i);
            if (relevantDocumentIds.contains(documentId) && seen.add(documentId)) {
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

    private double calculateDcg(List<Integer> grades) {
        double dcg = 0.0;

        for (int i = 0; i < grades.size(); i++) {
            int grade = grades.get(i);

            double gain = Math.pow(2, grade) - 1;
            double discount = Math.log(i + 2) / Math.log(2);

            dcg += gain / discount;
        }

        return dcg;
    }

}