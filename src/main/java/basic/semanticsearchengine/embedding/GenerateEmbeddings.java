package basic.semanticsearchengine.embedding;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

public interface GenerateEmbeddings {
    double[] getEmbeddingResult(String text) throws IOException, InterruptedException;
}
