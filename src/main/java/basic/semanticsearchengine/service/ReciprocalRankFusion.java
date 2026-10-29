package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ReciprocalRankFusion {

    private static final int DEFAULT_RANK_CONSTANT = 60;

    private final int rankConstant;

    public ReciprocalRankFusion() {
        this(DEFAULT_RANK_CONSTANT);
    }

    public ReciprocalRankFusion(int rankConstant) {
        if (rankConstant <= 0) {
            throw new IllegalArgumentException(
                    "rankConstant must be greater than 0");
        }

        this.rankConstant = rankConstant;
    }

    public List<SearchResult> fuse(
            List<SearchResult> semanticResults,
            List<SearchResult> keywordResults,
            int limit) {

        if (semanticResults == null || keywordResults == null) {
            throw new IllegalArgumentException(
                    "Search result lists must not be null");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be greater than 0");
        }

        Map<String, Double> fusedScores = new HashMap<>();
        Map<String, SearchResult> documents = new HashMap<>();

        accumulateScores(semanticResults, fusedScores, documents);
        accumulateScores(keywordResults, fusedScores, documents);

        return fusedScores.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Double>comparingByValue()
                                .reversed()
                                .thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> new SearchResult(
                        documents.get(entry.getKey()).getDocument(),
                        entry.getValue()))
                .toList();
    }

    private void accumulateScores(
            List<SearchResult> results,
            Map<String, Double> fusedScores,
            Map<String, SearchResult> documents) {

        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            String content = result.getDocument().getText();

            documents.putIfAbsent(content, result);

            double contribution = 1.0 / (rankConstant + i + 1);

            fusedScores.merge(
                    content,
                    contribution,
                    Double::sum);
        }
    }
}