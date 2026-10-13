package basic.semanticsearchengine.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Document {

    private final String text;
    private final double[] embedding;

    @JsonCreator
    public Document(
            @JsonProperty("text") String text,
            @JsonProperty("embedding") double[] embedding) {
        this.text = text;
        this.embedding = embedding;
    }

    public String getText() {
        return text;
    }

    public double[] getEmbedding() {
        return embedding;
    }
}
