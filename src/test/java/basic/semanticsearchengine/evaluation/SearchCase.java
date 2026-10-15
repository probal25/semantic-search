package basic.semanticsearchengine.evaluation;

import java.util.List;

public record SearchCase(
        String query,
        List<String> relevantDocumentIds
) {
}