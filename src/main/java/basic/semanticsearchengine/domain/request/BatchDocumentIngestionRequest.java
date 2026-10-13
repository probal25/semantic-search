package basic.semanticsearchengine.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchDocumentIngestionRequest {

    @NotEmpty(message = "Documents list must not be empty")
    @Size(max = 100, message = "A batch cannot exceed 100 documents")
    @Valid
    private List<DocumentIngestionRequest> documents;

    public BatchDocumentIngestionRequest() {
    }

    public List<DocumentIngestionRequest> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentIngestionRequest> documents) {
        this.documents = documents;
    }
}