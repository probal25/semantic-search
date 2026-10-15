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

    @Value("${search.min-similarity:0.50}")
    private double minSimilarity;

    public SearchService(
            GenerateEmbeddings embeddingClient,
            DocumentRepository repository) {
        this.embeddingClient = embeddingClient;
        this.repository = repository;
    }

    public List<SearchResult> search(String query, int limit)
            throws IOException, InterruptedException, SQLException {

        double[] queryVector = embeddingClient.getEmbeddingResult(query);

        return repository.search(queryVector, limit, minSimilarity);
    }

    public List<SearchResult> search(
            String query,
            int limit,
            double threshold)
            throws IOException, InterruptedException, SQLException {

        double[] queryVector = embeddingClient.getEmbeddingResult(query);

        return repository.search(queryVector, limit, threshold);
    }
}