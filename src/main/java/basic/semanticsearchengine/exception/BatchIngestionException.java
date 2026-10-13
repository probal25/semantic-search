package basic.semanticsearchengine.exception;

public class BatchIngestionException extends RuntimeException {

    private final int total;
    private final int created;
    private final int duplicates;
    private final int failedIndex;

    public BatchIngestionException(
            String message,
            int total,
            int created,
            int duplicates,
            int failedIndex,
            Throwable cause) {
        super(message, cause);
        this.total = total;
        this.created = created;
        this.duplicates = duplicates;
        this.failedIndex = failedIndex;
    }

    public int getTotal() {
        return total;
    }

    public int getCreated() {
        return created;
    }

    public int getDuplicates() {
        return duplicates;
    }

    public int getFailedIndex() {
        return failedIndex;
    }
}
