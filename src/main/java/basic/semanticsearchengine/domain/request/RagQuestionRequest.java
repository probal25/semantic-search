package basic.semanticsearchengine.domain.request;

import jakarta.validation.constraints.NotBlank;

public record RagQuestionRequest(
        @NotBlank(message = "Question must not be blank")
        String question
) {
}