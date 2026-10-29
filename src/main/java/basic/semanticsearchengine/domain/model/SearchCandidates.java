package basic.semanticsearchengine.domain.model;

import java.util.List;

public record SearchCandidates(
        double[] queryEmbedding,
        List<SearchResult> results) {

    public SearchCandidates {
        queryEmbedding = queryEmbedding.clone();
        results = List.copyOf(results);
    }

    @Override
    public double[] queryEmbedding() {
        return queryEmbedding.clone();
    }
}
