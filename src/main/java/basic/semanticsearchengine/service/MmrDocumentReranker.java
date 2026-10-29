
package basic.semanticsearchengine.service;

import org.springframework.stereotype.Component;
import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

@Component
public class MmrDocumentReranker {

    private final CosineSimilarity cosineSimilarity;

    public MmrDocumentReranker(
            CosineSimilarity cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }


    public List<SearchResult> rerank(
            double[] queryEmbedding,
            List<SearchResult> candidates,
            int limit,
            double lambda) {

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new IllegalArgumentException(
                    "Query embedding must not be empty");
        }

        if (candidates == null) {
            throw new IllegalArgumentException(
                    "Candidates must not be null");
        }

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Limit must be greater than 0");
        }

        if (lambda < 0.0 || lambda > 1.0
                || !Double.isFinite(lambda)) {
            throw new IllegalArgumentException(
                    "Lambda must be between 0 and 1");
        }

        List<SearchResult> remaining =
                new ArrayList<>(candidates);

        List<SearchResult> selected = new ArrayList<>();

        while (!remaining.isEmpty() && selected.size() < limit) {
            SearchResult bestCandidate = null;
            double bestMmrScore = Double.NEGATIVE_INFINITY;

            for (SearchResult candidate : remaining) {
                Document document = candidate.getDocument();

                double relevance = cosineSimilarity.calculate(
                        queryEmbedding,
                        document.getEmbedding());

                double redundancy = 0.0;

                for (SearchResult chosen : selected) {
                    double similarity = cosineSimilarity.calculate(
                            document.getEmbedding(),
                            chosen.getDocument().getEmbedding());

                    redundancy = Math.max(redundancy, similarity);
                }

                double mmrScore =
                        lambda * relevance
                                - (1.0 - lambda) * redundancy;

                if (bestCandidate == null
                        || mmrScore > bestMmrScore) {
                    bestCandidate = candidate;
                    bestMmrScore = mmrScore;
                }
            }

            selected.add(new SearchResult(
                    bestCandidate.getDocument(),
                    bestCandidate.getScore(),
                    bestMmrScore));

            remaining.remove(bestCandidate);
        }

        return List.copyOf(selected);
    }

}
