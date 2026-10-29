package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.Document;
import basic.semanticsearchengine.domain.model.SearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReciprocalRankFusionTest {

    private final ReciprocalRankFusion fusion =
            new ReciprocalRankFusion();

    private SearchResult result(String content) {
        return new SearchResult(
                new Document(content, new double[0]),
                0.5);
    }

    @Test
    void documentRankedHighlyInBothListsComesFirst() {
        List<SearchResult> semantic = List.of(
                result("Payment timeout"),
                result("Refund policy"));

        List<SearchResult> keyword = List.of(
                result("Refund policy"),
                result("Payment timeout"));

        List<SearchResult> fused =
                fusion.fuse(semantic, keyword, 10);

        assertEquals(2, fused.size());
        assertEquals(
                "Payment timeout",
                fused.get(0).getDocument().getText());
        assertEquals(
                "Refund policy",
                fused.get(1).getDocument().getText());
    }

    @Test
    void duplicateDocumentsAreCombinedIntoOneResult() {
        List<SearchResult> semantic = List.of(
                result("Payment timeout"));

        List<SearchResult> keyword = List.of(
                result("Payment timeout"));

        List<SearchResult> fused =
                fusion.fuse(semantic, keyword, 10);

        assertEquals(1, fused.size());
    }

    @Test
    void respectsResultLimit() {
        List<SearchResult> semantic = List.of(
                result("A"),
                result("B"),
                result("C"));

        List<SearchResult> fused =
                fusion.fuse(semantic, List.of(), 2);

        assertEquals(2, fused.size());
    }

    @Test
    void rejectsInvalidLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> fusion.fuse(List.of(), List.of(), 0));
    }
}
