package basic.semanticsearchengine.repository;

import basic.semanticsearchengine.domain.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class EmbeddingStore {

    private final Path filePath;
    private final ObjectMapper mapper;

    public EmbeddingStore(Path filePath) {
        this.filePath = filePath;
        this.mapper = new ObjectMapper();
    }

    public void save(List<Document> documents) throws IOException {
        Path parent = filePath.getParent();

        if (parent != null) {
            java.nio.file.Files.createDirectories(parent);
        }

        mapper.writeValue(filePath.toFile(), documents);
    }

    public List<Document> load() throws IOException {
        return mapper.readValue(
                filePath.toFile(),
                new TypeReference<List<Document>>() {}
        );
    }
}
