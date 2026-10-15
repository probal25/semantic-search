package basic.semanticsearchengine.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


@Component
public class EvaluationDatasetLoader {

    private final ObjectMapper objectMapper;

    public EvaluationDatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<SearchCase> loadSearchCases() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(
                "/evaluation/search-cases.json")) {

            if (input == null) {
                throw new IOException("Could not find search-cases.json");
            }

            return objectMapper.readValue(
                    input,
                    new TypeReference<List<SearchCase>>() {}
            );
        }
    }

    public List<EvaluationDocument> loadDocuments() throws IOException {
        try (InputStream input = getClass().getResourceAsStream(
                "/evaluation/documents.json")) {

            if (input == null) {
                throw new IOException("Could not find documents.json");
            }

            return objectMapper.readValue(
                    input,
                    new TypeReference<List<EvaluationDocument>>() {}
            );
        }
    }
}