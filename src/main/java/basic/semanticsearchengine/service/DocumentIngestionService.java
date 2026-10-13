package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.exception.BatchIngestionException;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import basic.semanticsearchengine.domain.response.BatchIngestionResponse;
import java.util.List;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class DocumentIngestionService {

    private static final int EXPECTED_EMBEDDING_DIMENSIONS = 768;

    private final GenerateEmbeddings embeddingClient;
    private final DocumentRepository repository;

    public DocumentIngestionService(
            GenerateEmbeddings embeddingClient,
            DocumentRepository repository) {
        this.embeddingClient = embeddingClient;
        this.repository = repository;
    }

    public boolean ingest(String text)
            throws IOException, InterruptedException, SQLException {

        // 1. Avoid generating an embedding for content already stored.
        if (repository.existsByContent(text)) {
            return false;
        }

        // 2. Generate an embedding for new content.
        double[] embedding;

        try {
            embedding = embeddingClient.getEmbeddingResult(text);
        } catch (IOException exception) {
            throw new EmbeddingGenerationException(
                    "Failed to generate document embedding",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new EmbeddingGenerationException(
                    "Document embedding generation was interrupted",
                    exception
            );
        }

        // 3. Validate the embedding dimensions.
        if (embedding == null || embedding.length != EXPECTED_EMBEDDING_DIMENSIONS) {
            throw new IllegalStateException(
                    "Expected embeddings with " +
                            EXPECTED_EMBEDDING_DIMENSIONS + " dimensions"
            );
        }

        // 4. Save the new document.
        return repository.save(new Document(text, embedding));
    }

    public BatchIngestionResponse ingestBatch(List<String> texts)
            throws IOException, InterruptedException, SQLException {

        int created = 0;
        int duplicates = 0;

        for (int i = 0; i < texts.size(); i++) {
            try {
                boolean inserted = ingest(texts.get(i));

                if (inserted) {
                    created++;
                } else {
                    duplicates++;
                }

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                throw new BatchIngestionException(
                        "Batch ingestion was interrupted at document index " + i,
                        texts.size(),
                        created,
                        duplicates,
                        i,
                        exception
                );

            } catch (Exception exception) {
                throw new BatchIngestionException(
                        "Batch ingestion failed at document index " + i,
                        texts.size(),
                        created,
                        duplicates,
                        i,
                        exception
                );
            }
        }

        return new BatchIngestionResponse(texts.size(), created, duplicates);
    }

}