package basic.semanticsearchengine.repository;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.exception.DatabaseOperationException;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DocumentRepositoryTest {

    @Test
    void existsByContentWrapsDatabaseConnectionFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);

        when(dataSource.getConnection())
                .thenThrow(new SQLException("Connection refused"));

        DocumentRepository repository = new DocumentRepository(dataSource);

        DatabaseOperationException exception = assertThrows(
                DatabaseOperationException.class,
                () -> repository.existsByContent("My bank transfer is stuck")
        );

        assertEquals(
                "Failed to check whether document exists",
                exception.getMessage()
        );
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Connection refused", exception.getCause().getMessage());
    }

    @Test
    void existsByContentReturnsTrueWhenDocumentExists() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getBoolean(1)).thenReturn(true);

        DocumentRepository repository = new DocumentRepository(dataSource);

        assertTrue(repository.existsByContent("Existing document"));
    }

    @Test
    void saveWrapsDatabaseFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate())
                .thenThrow(new SQLException("Insert failed"));

        DocumentRepository repository = new DocumentRepository(dataSource);
        Document document = new Document(
                "My bank transfer is stuck",
                new double[768]
        );

        DatabaseOperationException exception = assertThrows(
                DatabaseOperationException.class,
                () -> repository.save(document)
        );

        assertEquals("Failed to save document", exception.getMessage());
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Insert failed", exception.getCause().getMessage());
    }

    @Test
    void saveReturnsTrueWhenDocumentIsInserted() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        DocumentRepository repository = new DocumentRepository(dataSource);
        Document document = new Document(
                "New document",
                new double[768]
        );

        assertTrue(repository.save(document));
        verify(statement).executeUpdate();
    }

    @Test
    void searchWrapsDatabaseFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery())
                .thenThrow(new SQLException("Search query failed"));

        DocumentRepository repository = new DocumentRepository(dataSource);

        DatabaseOperationException exception = assertThrows(
                DatabaseOperationException.class,
                () -> repository.search(new double[768], 5)
        );

        assertEquals("Failed to search documents", exception.getMessage());
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Search query failed", exception.getCause().getMessage());
    }

    @Test
    void searchReturnsDocumentsWithSimilarityScores() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("content"))
                .thenReturn("How to transfer money?");
        when(resultSet.getDouble("similarity")).thenReturn(0.95);

        DocumentRepository repository = new DocumentRepository(dataSource);

        List<SearchResult> results = repository.search(new double[768], 5);

        assertEquals(1, results.size());
        assertEquals(
                "How to transfer money?",
                results.get(0).getDocument().getText()
        );
        assertEquals(0.95, results.get(0).getScore(), 0.0001);
    }

    @Test
    void updateEmbeddingWrapsDatabaseFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate())
                .thenThrow(new SQLException("Update failed"));

        DocumentRepository repository = new DocumentRepository(dataSource);

        DatabaseOperationException exception = assertThrows(
                DatabaseOperationException.class,
                () -> repository.updateEmbedding(
                        "My bank transfer is stuck",
                        new double[768]
                )
        );

        assertEquals(
                "Failed to update document embedding",
                exception.getMessage()
        );
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Update failed", exception.getCause().getMessage());
    }

    @Test
    void findAllWrapsDatabaseFailure() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery())
                .thenThrow(new SQLException("Read failed"));

        DocumentRepository repository = new DocumentRepository(dataSource);

        DatabaseOperationException exception = assertThrows(
                DatabaseOperationException.class,
                repository::findAll
        );

        assertEquals(
                "Failed to retrieve documents",
                exception.getMessage()
        );
        assertInstanceOf(SQLException.class, exception.getCause());
        assertEquals("Read failed", exception.getCause().getMessage());
    }
}