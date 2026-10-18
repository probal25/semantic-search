package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
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

    @Value("${search.min-similarity:0.50}")
    private double minSimilarity;

    @Value("${search.reranking.enabled:false}")
    private boolean rerankingEnabled;

    @Value("${search.candidate-limit:10}")
    private int candidateLimit;

    public SearchService(
            GenerateEmbeddings embeddingClient,
            DocumentRepository repository,
            DocumentReranker reranker) {
        this.embeddingClient = embeddingClient;
        this.repository = repository;
        this.reranker = reranker;
    }

    public List<SearchResult> search(String query, int limit)
            throws IOException, InterruptedException, SQLException {

        return search(query, limit, minSimilarity);
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

        double[] queryVector =
                embeddingClient.getEmbeddingResult(query);

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
}