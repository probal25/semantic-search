package basic.semanticsearchengine.domain.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class SearchRequest {

    @NotBlank(message = "Query must not be blank")
    private String query;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 20, message = "Limit must not exceed 20")
    private int limit = 5;

    public SearchRequest() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
