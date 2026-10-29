package basic.semanticsearchengine.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (first, second) -> first
                ));

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Validation failed",
                "details", errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DatabaseOperationException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseErrors(
            DatabaseOperationException ex) {

        logger.error("Database operation failed", ex);

        Map<String, Object> response = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Database operation failed",
                "message", "The request could not be completed"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestBody(
            HttpMessageNotReadableException ex) {

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", "Request body is missing or contains invalid JSON"
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(EmbeddingGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleEmbeddingFailure(
            EmbeddingGenerationException ex) {

        logger.error("Embedding generation failed", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "status", HttpStatus.BAD_GATEWAY.value(),
                        "error", "Embedding generation failed",
                        "message", "The document could not be processed"
                ));
    }

    @ExceptionHandler(BatchIngestionException.class)
    public ResponseEntity<Map<String, Object>> handleBatchIngestionException(
            BatchIngestionException exception) {

        boolean embeddingFailure =
                hasCause(exception, EmbeddingGenerationException.class);

        HttpStatus status = embeddingFailure
                ? HttpStatus.BAD_GATEWAY
                : HttpStatus.INTERNAL_SERVER_ERROR;

        if (embeddingFailure) {
            logger.error("Batch embedding generation failed", exception);
        } else {
            logger.error("Batch ingestion failed", exception);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", embeddingFailure
                ? "Batch embedding generation failed"
                : "Batch ingestion failed");
        body.put("message",
                "Processing stopped before the batch was completed");
        body.put("total", exception.getTotal());
        body.put("created", exception.getCreated());
        body.put("duplicates", exception.getDuplicates());
        body.put("failedIndex", exception.getFailedIndex());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedErrors(
            Exception ex) {

        logger.error("Unexpected application error", ex);

        Map<String, Object> response = Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal server error",
                "message", "The request could not be completed"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private boolean hasCause(
            Throwable exception,
            Class<? extends Throwable> type) {

        Throwable current = exception;

        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}