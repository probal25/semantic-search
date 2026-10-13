package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.response.BatchIngestionResponse;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.exception.BatchIngestionException;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.repository.DocumentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceBatchTest {

    @Mock
    private GenerateEmbeddings embeddingClient;

    @Mock
    private DocumentRepository repository;

    private DocumentIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ingestionService = new DocumentIngestionService(
                embeddingClient,
                repository
        );
    }

    @Test
    void ingestBatchCountsCreatedDocumentsAndDuplicates() throws Exception {
        when(embeddingClient.getEmbeddingResult(any(String.class)))
                .thenReturn(new double[768]);

        when(repository.save(any(Document.class)))
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true);

        BatchIngestionResponse response = ingestionService.ingestBatch(
                List.of("Document A", "Document B", "Document C")
        );

        assertEquals(3, response.getTotal());
        assertEquals(2, response.getCreated());
        assertEquals(1, response.getDuplicates());

        verify(embeddingClient, times(3))
                .getEmbeddingResult(any(String.class));
        verify(repository, times(3)).save(any(Document.class));
    }

    @Test
    void ingestBatchWithNoDocumentsReturnsZeroCounts() throws Exception {
        BatchIngestionResponse response =
                ingestionService.ingestBatch(List.of());

        assertEquals(0, response.getTotal());
        assertEquals(0, response.getCreated());
        assertEquals(0, response.getDuplicates());

        verifyNoInteractions(embeddingClient, repository);
    }

    @Test
    void ingestBatchStopsWhenEmbeddingGenerationFails() throws Exception {
        when(embeddingClient.getEmbeddingResult("Document A"))
                .thenReturn(new double[768]);

        when(embeddingClient.getEmbeddingResult("Document B"))
                .thenThrow(new IOException("Ollama unavailable"));

        // Document A is successfully inserted before B fails.
        when(repository.save(any(Document.class))).thenReturn(true);

        BatchIngestionException exception = assertThrows(
                BatchIngestionException.class,
                () -> ingestionService.ingestBatch(
                        List.of("Document A", "Document B", "Document C")
                )
        );

        assertEquals(3, exception.getTotal());
        assertEquals(1, exception.getCreated());
        assertEquals(0, exception.getDuplicates());
        assertEquals(1, exception.getFailedIndex());

        assertInstanceOf(
                EmbeddingGenerationException.class,
                exception.getCause()
        );

        verify(repository, times(1)).save(any(Document.class));

        verify(embeddingClient, never())
                .getEmbeddingResult("Document C");
    }
}