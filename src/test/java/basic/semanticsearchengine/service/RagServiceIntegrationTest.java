
package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.response.RagResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RagServiceIntegrationTest {

    @Autowired
    private RagService ragService;

    @Test
    void shouldAnswerQuestionUsingRetrievedDocuments()
            throws Exception {

        String question =
                "Why might a payment get stuck or time out?";

        RagResponse response = ragService.answer(question);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
        assertFalse(response.getAnswer().isBlank());

        System.out.println("\nQuestion: " + question);
        System.out.println("RAG answer: " + response.getAnswer());

        System.out.println("\nRetrieved sources:");

        response.getSources().forEach(source ->
                System.out.println(
                        "Score: " + source.getScore()
                                + " | Text: " + source.getText()
                )
        );
    }
}
