package basic.semanticsearchengine.evaluation;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EvaluationDatasetValidator {

    public void validate(
            List<EvaluationDocument> documents,
            List<SearchCase> searchCases) {

        Set<String> documentIds = new HashSet<>();
        Set<String> documentContents = new HashSet<>();

        for (EvaluationDocument document : documents) {
            if (!documentIds.add(document.id())) {
                throw new IllegalArgumentException(
                        "Duplicate evaluation document ID: " + document.id());
            }

            if (!documentContents.add(document.content())) {
                throw new IllegalArgumentException(
                        "Duplicate evaluation document content: "
                                + document.content());
            }
        }

        for (SearchCase searchCase : searchCases) {
            for (String relevantId : searchCase.relevantDocumentIds()) {
                if (!documentIds.contains(relevantId)) {
                    throw new IllegalArgumentException(
                            "Search case references unknown document ID: "
                                    + relevantId
                                    + " | Query: " + searchCase.query());
                }
            }
        }
    }
}
