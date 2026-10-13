package basic.semanticsearchengine.domain.response;

public class BatchIngestionErrorResponse {

    private final String error;
    private final String message;

    public BatchIngestionErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}