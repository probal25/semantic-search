package basic.semanticsearchengine.domain.response;

public class BatchIngestionResponse {

    private final int total;
    private final int created;
    private final int duplicates;

    public BatchIngestionResponse(int total, int created, int duplicates) {
        this.total = total;
        this.created = created;
        this.duplicates = duplicates;
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
}