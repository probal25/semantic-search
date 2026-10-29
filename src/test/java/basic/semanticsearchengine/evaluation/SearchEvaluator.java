package basic.semanticsearchengine.evaluation;

import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.repository.DocumentRepository;
import basic.semanticsearchengine.service.DocumentIngestionService;
import basic.semanticsearchengine.service.SearchService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@Component
public class SearchEvaluator {

    private final EvaluationDatasetLoader datasetLoader;
    private final DocumentIngestionService ingestionService;
    private final DocumentRepository repository;
    private final SearchService searchService;
    private final SearchMetrics metrics;
    private final EvaluationDatasetValidator datasetValidator;

    public SearchEvaluator(
            EvaluationDatasetLoader datasetLoader,
            DocumentIngestionService ingestionService,
            DocumentRepository repository,
            SearchService searchService,
            EvaluationDatasetValidator datasetValidator) {
        this.datasetLoader = datasetLoader;
        this.ingestionService = ingestionService;
        this.repository = repository;
        this.searchService = searchService;
        this.datasetValidator = datasetValidator;
        this.metrics = new SearchMetrics();
    }

    public void evaluate(int k)
            throws IOException, InterruptedException, SQLException {

        List<EvaluationDocument> documents =
                datasetLoader.loadDocuments();

        List<SearchCase> searchCases =
                datasetLoader.loadSearchCases();

        datasetValidator.validate(documents, searchCases);

        EvaluationDocumentIndex index =
                new EvaluationDocumentIndex(documents);

        List<String> insertedByThisRun = new ArrayList<>();

        Map<Double, Double> precisionTotals = new LinkedHashMap<>();
        Map<Double, Double> reciprocalRankTotals = new LinkedHashMap<>();
        Map<Double, Double> recallTotals = new LinkedHashMap<>();
        Map<Double, Double> returnedPrecisionTotals = new LinkedHashMap<>();
        Map<Double, Double> precisionAt1Totals = new LinkedHashMap<>();
        Map<Double, Double> ndcgTotals = new LinkedHashMap<>();

        List<Double> thresholds = List.of(0.5);
//        List<Double> thresholds = List.of(0.0, 0.4, 0.5, 0.6);

        try {
            // Ingest only documents that do not already exist.
            for (EvaluationDocument document : documents) {
                boolean inserted = ingestionService.ingest(document.content());

                if (inserted) {
                    insertedByThisRun.add(document.content());
                }
            }

            for (double threshold : thresholds) {
                System.out.printf(
                        "%n========== Threshold: %.2f ==========%n",
                        threshold);

                int emptyResultCount = 0;

                for (SearchCase searchCase : searchCases) {
                    List<SearchResult> results =
                            searchService.search(
                                    searchCase.query(), k, threshold);

                    double highestScore = results.isEmpty()
                            ? Double.NaN
                            : results.get(0).getScore();

                    // Count queries that return no documents.
                    if (results.isEmpty()) {
                        emptyResultCount++;
                    }

                    for (int i = 0; i < results.size(); i++) {
                        SearchResult result = results.get(i);

                        String documentId = index.findIdByContent(
                                result.getDocument().getText());

                        int relevanceGrade = searchCase.relevanceGrades()
                                .getOrDefault(documentId, 0);

                        System.out.printf(
                                "Rank %d | Cosine: %.4f | Grade: %d | ID: %s | Document: %s%n",
                                i + 1,
                                result.getScore(),
                                relevanceGrade,
                                documentId,
                                result.getDocument().getText()
                        );
                    }

                    System.out.printf(
                            "Highest score: %.4f%n",
                            highestScore);

                    List<String> retrievedIds = results.stream()
                            .map(result -> index.findIdByContent(
                                    result.getDocument().getText()))
                            .toList();

                    Set<String> relevantIds =
                            new HashSet<>(searchCase.relevantDocumentIds());

                    Map<String, Integer> relevanceGrades =
                            searchCase.relevanceGrades();

                    double ndcg = relevanceGrades.values().stream()
                            .anyMatch(grade -> grade > 0)
                            ? metrics.ndcgAtK(retrievedIds, relevanceGrades, k)
                            : Double.NaN;

                    if (!Double.isNaN(ndcg)) {
                        ndcgTotals.merge(threshold, ndcg, Double::sum);
                    }

                    double precision =
                            metrics.precisionAtK(retrievedIds, relevantIds, k);

                    double recall = relevantIds.isEmpty()
                            ? Double.NaN
                            : metrics.recallAtK(retrievedIds, relevantIds, k);

                    double precisionAt1 =
                            metrics.precisionAtK(retrievedIds, relevantIds, 1);

                    double reciprocalRank = relevantIds.isEmpty()
                            ? Double.NaN
                            : metrics.reciprocalRank(retrievedIds, relevantIds);

                    if (!Double.isNaN(reciprocalRank)) {
                        reciprocalRankTotals.merge(
                                threshold, reciprocalRank, Double::sum);
                    }

                    double returnedPrecision =
                            SearchMetrics.precisionAmongReturned(
                                    retrievedIds, relevantIds);

                    precisionTotals.merge(threshold, precision, Double::sum);

                    if (!Double.isNaN(recall)) {
                        recallTotals.merge(threshold, recall, Double::sum);
                    }

                    returnedPrecisionTotals.merge(
                            threshold, returnedPrecision, Double::sum);

                    precisionAt1Totals.merge(
                            threshold, precisionAt1, Double::sum);

                    System.out.printf(
                            "Query: %s%n" +
                                    "Precision@%d: %.3f%n" +
                                    "Recall@%d: %.3f%n" +
                                    "Precision among returned: %.3f%n" +
                                    "NDCG@%d: %.3f%n%n",
                            searchCase.query(),
                            k, precision,
                            k, recall,
                            returnedPrecision,
                            k, ndcg
                    );
                }

                int queryCount = searchCases.size();

                long recallQueryCount = searchCases.stream()
                        .filter(searchCase ->
                                !searchCase.relevantDocumentIds().isEmpty())
                        .count();

                long mrrQueryCount = searchCases.stream()
                        .filter(searchCase ->
                                !searchCase.relevantDocumentIds().isEmpty())
                        .count();

                double averageRecall = recallQueryCount == 0
                        ? Double.NaN
                        : recallTotals.getOrDefault(threshold, 0.0)
                        / recallQueryCount;

                double averageMrr = mrrQueryCount == 0
                        ? Double.NaN
                        : reciprocalRankTotals.getOrDefault(threshold, 0.0)
                        / mrrQueryCount;

                long ndcgQueryCount = searchCases.stream()
                        .filter(searchCase ->
                                searchCase.relevanceGrades().values().stream()
                                        .anyMatch(grade -> grade > 0))
                        .count();

                double averageNdcg = ndcgQueryCount == 0
                        ? Double.NaN
                        : ndcgTotals.getOrDefault(threshold, 0.0)
                        / ndcgQueryCount;

                double emptyResultRate =
                        100.0 * emptyResultCount / queryCount;

                System.out.printf(
                        "SUMMARY | Threshold: %.2f | " +
                                "Avg Precision@1: %.3f | " +
                                "Avg Precision@%d: %.3f | " +
                                "Avg Recall@%d: %.3f | " +
                                "MRR: %.3f | " +
                                "Avg Precision among returned: %.3f | " +
                                "Avg NDCG@6: %.3f | " +
                                "Empty-result rate: %.1f%%%n",
                        threshold,
                        precisionAt1Totals.get(threshold) / queryCount,
                        k,
                        precisionTotals.get(threshold) / queryCount,
                        k,
                        averageRecall,
                        averageMrr,
                        returnedPrecisionTotals.get(threshold) / queryCount,
                        averageNdcg,
                        emptyResultRate);
            }
        } finally {
            // Delete only documents inserted by this evaluation run.
            for (String content : insertedByThisRun) {
                repository.deleteByContent(content);
            }
        }
    }
}