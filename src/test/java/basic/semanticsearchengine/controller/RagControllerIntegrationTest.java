
package basic.semanticsearchengine.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void answerReturnsGeneratedAnswerAndSources()
            throws Exception {

        String request = """
                {
                    "question": "Why might a payment get stuck or time out?"
                }
                """;

        mockMvc.perform(post("/api/rag/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.answer").isNotEmpty())
                .andExpect(jsonPath("$.sources").isArray())
                .andExpect(jsonPath("$.sources.length()").value(3))
                .andExpect(jsonPath("$.sources[0].text").isNotEmpty())
                .andExpect(jsonPath("$.sources[0].score").isNumber());
    }

    @Test
    void answerReturnsBadRequestWhenQuestionIsBlank()
            throws Exception {

        String request = """
                {
                    "question": "   "
                }
                """;

        mockMvc.perform(post("/api/rag/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.details.question")
                        .value("Question must not be blank"));
    }

    @Test
    void answerReturnsBadRequestWhenQuestionIsMissing()
            throws Exception {

        String request = "{}";

        mockMvc.perform(post("/api/rag/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.details.question")
                        .value("Question must not be blank"));
    }

    @Test
    void answerReturnsBadRequestWhenJsonIsMalformed()
            throws Exception {

        mockMvc.perform(post("/api/rag/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}
