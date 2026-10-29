package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchCandidates;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
public class SearchService {

    private final GenerateEmbeddings embeddingClient;
    private final DocumentRepository repository;
    private final DocumentReranker reranker;
    private final ReciprocalRankFusion reciprocalRankFusion;

    @Value("${search.min-similarity:0.50}")
    private double minSimilarity;

    @Value("${search.reranking.enabled:false}")
    private boolean rerankingEnabled;

    @Value("${search.candidate-limit:10}")
    private int candidateLimit;

    public SearchService(
            GenerateEmbeddings embeddingClient,
            DocumentRepository repository,
            DocumentReranker reranker, ReciprocalRankFusion reciprocalRankFusion) {
        this.embeddingClient = embeddingClient;
        this.repository = repository;
        this.reranker = reranker;
        this.reciprocalRankFusion = reciprocalRankFusion;
    }

    public List<SearchResult> search(String query, int limit)
            throws IOException, InterruptedException, SQLException {

//        return search(query, limit, minSimilarity);
        List<SearchResult> results = hybridSearch(query, limit);

        results.forEach(result ->
                System.out.println(
                        result.getDocument().getText()
                                + " | score: " + result.getScore()));

        return results;
    }

    public List<SearchResult> search(
            String query,
            int limit,
            double threshold)
            throws IOException, InterruptedException, SQLException {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be greater than 0");
        }

        double[] queryVector = generateQueryEmbedding(query);

        if (!rerankingEnabled) {
            return repository.search(queryVector, limit, threshold);
        }

        int candidatesToFetch = Math.max(limit, candidateLimit);

        List<SearchResult> candidates =
                repository.search(queryVector, candidatesToFetch, threshold);

        return reranker.rerank(query, candidates)
                .stream()
                .limit(limit)
                .toList();
    }

    public List<SearchResult> hybridSearch(String query, int limit)
            throws IOException, InterruptedException, SQLException {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be greater than 0");
        }

        SearchCandidates candidates =
                retrieveCandidates(
                        query,
                        Math.max(limit, candidateLimit));

        return candidates.results()
                .stream()
                .limit(limit)
                .toList();
    }

    public SearchCandidates retrieveCandidates(
            String query,
            int candidateCount)
            throws IOException, InterruptedException, SQLException {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "query must not be blank");
        }

        if (candidateCount <= 0) {
            throw new IllegalArgumentException(
                    "candidateCount must be greater than 0");
        }

        double[] queryVector = generateQueryEmbedding(query);

        List<SearchResult> semanticResults =
                repository.search(
                        queryVector,
                        candidateCount,
                        minSimilarity);

        List<SearchResult> keywordResults =
                repository.searchByKeyword(
                        query,
                        candidateCount);

        List<SearchResult> candidates =
                reciprocalRankFusion.fuse(
                        semanticResults,
                        keywordResults,
                        candidateCount);

        return new SearchCandidates(queryVector, candidates);
    }

    private double[] generateQueryEmbedding(String query)
            throws InterruptedException {

        try {
            return embeddingClient.getEmbeddingResult(query);
        } catch (IOException | RuntimeException ex) {
            throw new EmbeddingGenerationException(
                    "Failed to generate query embedding", ex);
        }
    }
}