package basic.semanticsearchengine.evaluation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class SearchEvaluatorTest {

    @Autowired
    private SearchEvaluator searchEvaluator;

    @Test
    void evaluatesSemanticSearchQualityLimitSix() {
        assertDoesNotThrow(() -> searchEvaluator.evaluate(6));
    }

    @Test
    void evaluatesSemanticSearchQualityLimitTen() {
        assertDoesNotThrow(() -> searchEvaluator.evaluate(10));
    }

    @Test
    void evaluatesSemanticSearchQualityLimitTwenty() {
        assertDoesNotThrow(() -> searchEvaluator.evaluate(20));
    }
}
