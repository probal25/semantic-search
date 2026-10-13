package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.domain.request.BatchDocumentIngestionRequest;
import basic.semanticsearchengine.domain.request.DocumentIngestionRequest;
import basic.semanticsearchengine.domain.response.BatchIngestionResponse;
import basic.semanticsearchengine.service.DocumentIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> ingest(
            @Valid @RequestBody DocumentIngestionRequest request)
            throws IOException, InterruptedException, SQLException {

        boolean created = ingestionService.ingest(request.getText());

        if (created) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of("message", "Document ingested successfully"));
        }

        return ResponseEntity
                .ok(Map.of("message", "Document already exists"));
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchIngestionResponse> ingestBatch(
            @Valid @RequestBody BatchDocumentIngestionRequest request)
            throws IOException, InterruptedException, SQLException {

        List<String> texts = request.getDocuments()
                .stream()
                .map(DocumentIngestionRequest::getText)
                .toList();

        BatchIngestionResponse response =
                ingestionService.ingestBatch(texts);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
