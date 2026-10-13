package basic.semanticsearchengine.repository;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.exception.DatabaseOperationException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DocumentRepository {

    private final DataSource dataSource;

    public DocumentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean save(Document document) {
        String sql = """
                INSERT INTO documents (content, embedding)
                VALUES (?, ?::vector)
                ON CONFLICT (content) DO NOTHING
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, document.getText());
            statement.setString(2, toVectorString(document.getEmbedding()));

            int rowsInserted = statement.executeUpdate();
            return rowsInserted == 1;

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to save document", exception);
        }
    }

    public List<Document> findAll() {
        String sql = """
                SELECT content, embedding::text
                FROM documents
                ORDER BY id
                """;

        List<Document> documents = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String content = resultSet.getString("content");
                String vectorText = resultSet.getString("embedding");

                documents.add(new Document(content, parseVector(vectorText)));
            }

            return documents;

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to retrieve documents", exception);
        }
    }

    public List<SearchResult> search(double[] queryVector, int limit) {
        String sql = """
                SELECT content,
                       1 - (embedding <=> CAST(? AS vector)) AS similarity
                FROM documents
                ORDER BY embedding <=> CAST(? AS vector)
                LIMIT ?
                """;

        String vector = toVectorString(queryVector);
        List<SearchResult> results = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, vector);
            statement.setString(2, vector);
            statement.setInt(3, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String content = resultSet.getString("content");
                    double similarity = resultSet.getDouble("similarity");

                    Document document = new Document(content, new double[0]);
                    results.add(new SearchResult(document, similarity));
                }
            }

            return results;

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to search documents", exception);
        }
    }

    public void updateEmbedding(String content, double[] embedding) {
        String sql = """
                UPDATE documents
                SET embedding = CAST(? AS vector)
                WHERE content = ?
                """;

        String vector = toVectorString(embedding);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, vector);
            statement.setString(2, content);

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new DatabaseOperationException(
                        "Expected to update one document, but updated "
                                + updatedRows,
                        null
                );
            }

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to update document embedding", exception);
        }
    }

    public boolean existsByContent(String content) {
        String sql = "SELECT EXISTS " +
                "(SELECT 1 FROM documents WHERE content = ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, content);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to check whether document exists", exception);
        }
    }

    public void deleteByContent(String content) {
        String sql = "DELETE FROM documents WHERE content = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, content);
            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new DatabaseOperationException(
                    "Failed to delete document", exception);
        }
    }

    private String toVectorString(double[] embedding) {
        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                result.append(",");
            }

            result.append(embedding[i]);
        }

        result.append("]");
        return result.toString();
    }

    private double[] parseVector(String vectorText) {
        String values = vectorText.substring(1, vectorText.length() - 1);

        if (values.isBlank()) {
            return new double[0];
        }

        String[] parts = values.split(",");
        double[] embedding = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Double.parseDouble(parts[i]);
        }

        return embedding;
    }
}