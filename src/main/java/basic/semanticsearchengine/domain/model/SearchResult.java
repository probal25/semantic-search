package basic.semanticsearchengine.domain.model;

public class SearchResult {

    private final Document document;
    private final double score;
    private final double rerankingScore;

    public SearchResult(Document document, double score) {
        this(document, score, Double.NaN);
    }

    public SearchResult(
            Document document,
            double score,
            double rerankingScore) {
        this.document = document;
        this.score = score;
        this.rerankingScore = rerankingScore;
    }

    public Document getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }

    public double getRerankingScore() {
        return rerankingScore;
    }
}
