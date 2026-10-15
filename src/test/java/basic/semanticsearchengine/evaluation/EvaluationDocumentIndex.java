package basic.semanticsearchengine.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluationDocumentIndex {

    private final Map<String, String> contentToId;

    public EvaluationDocumentIndex(List<EvaluationDocument> documents) {
        this.contentToId = new HashMap<>();

        for (EvaluationDocument document : documents) {
            contentToId.put(document.content(), document.id());
        }
    }

    public String findIdByContent(String content) {
        return contentToId.get(content);
    }
}