package basic.semanticsearchengine.domain.response;

public class SearchResponse {

    private final String text;
    private final double score;

    public SearchResponse(String text, double score) {
        this.text = text;
        this.score = score;
    }

    public String getText() {
        return text;
    }

    public double getScore() {
        return score;
    }
}