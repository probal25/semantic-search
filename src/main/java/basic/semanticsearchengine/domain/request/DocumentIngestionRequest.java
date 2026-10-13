package basic.semanticsearchengine.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DocumentIngestionRequest {

    @NotBlank(message = "Document text must not be blank")
    @Size(max = 10000, message = "Document text must not exceed 10000 characters")
    private String text;

    public DocumentIngestionRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}