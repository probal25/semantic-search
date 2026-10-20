package basic.semanticsearchengine.evaluation;

import java.util.List;
import java.util.Map;

public record SearchCase(
        String query,
        List<String> relevantDocumentIds,
        Map<String, Integer> relevanceGrades
) {
}