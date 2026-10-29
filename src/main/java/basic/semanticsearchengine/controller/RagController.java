package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.domain.request.RagQuestionRequest;
import basic.semanticsearchengine.domain.response.RagResponse;
import basic.semanticsearchengine.service.RagService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/answer")
    public ResponseEntity<RagResponse> answer(
            @Valid @RequestBody RagQuestionRequest request)
            throws IOException, InterruptedException, SQLException {

        String question = request.question();

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(ragService.answer(question));
    }
}