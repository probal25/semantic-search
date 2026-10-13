package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.embedding.GenerateEmbeddings;
import basic.semanticsearchengine.exception.EmbeddingGenerationException;
import basic.semanticsearchengine.repository.DocumentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

    @Mock
    private GenerateEmbeddings embeddingClient;

    @Mock
    private DocumentRepository repository;

    @InjectMocks
    private DocumentIngestionService ingestionService;

    @AfterEach
    void clearInterruptFlag() {
        // Avoid leaking the interruption status into another test
        // when testing interrupted embedding generation.
        Thread.interrupted();
    }

    @Test
    void ingestSavesDocumentWhenContentIsNew() throws Exception {
        String text = "My payment failed";

        double[] embedding = new double[768];
        embedding[0] = 1.0;

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text))
                .thenReturn(embedding);
        when(repository.save(any(Document.class))).thenReturn(true);

        boolean result = ingestionService.ingest(text);

        assertTrue(result);

        verify(repository).existsByContent(text);
        verify(embeddingClient).getEmbeddingResult(text);
        verify(repository).save(any(Document.class));
    }

    @Test
    void ingestSkipsEmbeddingGenerationForDuplicateContent()
            throws Exception {
        String text = "My payment failed";

        when(repository.existsByContent(text)).thenReturn(true);

        boolean result = ingestionService.ingest(text);

        assertFalse(result);

        verify(repository).existsByContent(text);
        verifyNoInteractions(embeddingClient);
        verify(repository, never()).save(any(Document.class));
    }

    @Test
    void ingestRejectsEmbeddingWithWrongDimensions() throws Exception {
        String text = "My payment failed";

        double[] invalidEmbedding = new double[100];

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text))
                .thenReturn(invalidEmbedding);

        assertThrows(
                IllegalStateException.class,
                () -> ingestionService.ingest(text)
        );

        verify(embeddingClient).getEmbeddingResult(text);
        verify(repository, never()).save(any(Document.class));
    }

    @Test
    void ingestRejectsNullEmbedding() throws Exception {
        String text = "My payment failed";

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text)).thenReturn(null);

        assertThrows(
                IllegalStateException.class,
                () -> ingestionService.ingest(text)
        );

        verify(repository, never()).save(any(Document.class));
    }

    @Test
    void ingestWrapsIOExceptionFromEmbeddingClient() throws Exception {
        String text = "My payment failed";

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text))
                .thenThrow(new IOException("Ollama is unavailable"));

        assertThrows(
                EmbeddingGenerationException.class,
                () -> ingestionService.ingest(text)
        );

        verify(repository, never()).save(any(Document.class));
    }

    @Test
    void ingestRestoresInterruptFlagWhenEmbeddingIsInterrupted()
            throws Exception {
        String text = "My payment failed";

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text))
                .thenThrow(new InterruptedException("Test interruption"));

        try {
            assertThrows(
                    EmbeddingGenerationException.class,
                    () -> ingestionService.ingest(text)
            );

            assertTrue(
                    Thread.currentThread().isInterrupted(),
                    "The interrupt status should be restored"
            );

            verify(repository, never()).save(any(Document.class));
        } finally {
            // Keep the test thread clean for subsequent tests.
            Thread.interrupted();
        }
    }

    @Test
    void ingestReturnsFalseWhenRepositoryDoesNotInsertDocument()
            throws Exception {
        String text = "A new payment document";

        double[] embedding = new double[768];

        when(repository.existsByContent(text)).thenReturn(false);
        when(embeddingClient.getEmbeddingResult(text))
                .thenReturn(embedding);
        when(repository.save(any(Document.class))).thenReturn(false);

        boolean result = ingestionService.ingest(text);

        assertFalse(result);

        verify(repository).save(any(Document.class));
    }
}
