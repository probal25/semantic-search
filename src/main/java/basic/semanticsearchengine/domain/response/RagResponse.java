package basic.semanticsearchengine.domain.response;

import java.util.List;

public class RagResponse {

    private final String answer;
    private final List<Source> sources;

    public RagResponse(String answer, List<Source> sources) {
        this.answer = answer;
        this.sources = List.copyOf(sources);
    }

    public String getAnswer() {
        return answer;
    }

    public List<Source> getSources() {
        return sources;
    }

    public static class Source {

        private final String text;
        private final double score;

        public Source(String text, double score) {
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
}
