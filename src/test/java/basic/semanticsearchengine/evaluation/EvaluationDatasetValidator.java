package basic.semanticsearchengine.evaluation;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

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
            Map<String, Integer> relevanceGrades = searchCase.relevanceGrades();

            for (String relevantId : searchCase.relevantDocumentIds()) {
                if (!documentIds.contains(relevantId)) {
                    throw new IllegalArgumentException(
                            "Search case references unknown document ID: "
                                    + relevantId
                                    + " | Query: " + searchCase.query());
                }
            }

            if (relevanceGrades == null) {
                throw new IllegalArgumentException(
                        "Missing relevance grades for query: " + searchCase.query());
            }

            Set<String> relevantIds =
                    new HashSet<>(searchCase.relevantDocumentIds());

            for (Map.Entry<String, Integer> entry : relevanceGrades.entrySet()) {
                String documentId = entry.getKey();
                Integer grade = entry.getValue();

                if (!documentIds.contains(documentId)) {
                    throw new IllegalArgumentException(
                            "Relevance grades reference unknown document ID: "
                                    + documentId
                                    + " | Query: " + searchCase.query());
                }

                if (grade == null || grade < 0 || grade > 3) {
                    throw new IllegalArgumentException(
                            "Relevance grade must be between 0 and 3 for document ID: "
                                    + documentId
                                    + " | Query: " + searchCase.query());
                }

                if (grade > 0 && !relevantIds.contains(documentId)) {
                    throw new IllegalArgumentException(
                            "Document with positive relevance grade must appear in "
                                    + "relevantDocumentIds: "
                                    + documentId
                                    + " | Query: " + searchCase.query());
                }

                if (grade == 0 && relevantIds.contains(documentId)) {
                    throw new IllegalArgumentException(
                            "Document with grade 0 must not appear in "
                                    + "relevantDocumentIds: "
                                    + documentId
                                    + " | Query: " + searchCase.query());
                }
            }
        }
    }
}
