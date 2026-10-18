package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class LexicalDocumentReranker implements DocumentReranker {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("[a-z0-9]+");

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "because",
            "by", "did", "do", "for", "from", "has", "have", "how",
            "i", "in", "is", "it", "my", "of", "on", "or", "the",
            "to", "was", "were", "what", "when", "why", "with", "yet"
    );

    @Override
    public List<SearchResult> rerank(
            String query,
            List<SearchResult> candidates) {

        Set<String> queryTokens = tokenize(query);

        List<SearchResult> reranked = new ArrayList<>();

        for (SearchResult candidate : candidates) {
            Set<String> documentTokens =
                    tokenize(candidate.getDocument().getText());

            double lexicalScore =
                    calculateOverlap(queryTokens, documentTokens);

            reranked.add(new SearchResult(
                    candidate.getDocument(),
                    candidate.getScore(),
                    lexicalScore
            ));
        }

        reranked.sort(
                Comparator.comparingDouble(SearchResult::getRerankingScore)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingDouble(SearchResult::getScore)
                                        .reversed()
                        )
        );

        return reranked;
    }

    private Set<String> tokenize(String text) {
        return TOKEN_PATTERN.matcher(text.toLowerCase(Locale.ROOT))
                .results()
                .map(match -> match.group())
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private double calculateOverlap(
            Set<String> queryTokens,
            Set<String> documentTokens) {

        if (queryTokens.isEmpty()) {
            return 0.0;
        }

        long matchingTokens = queryTokens.stream()
                .filter(documentTokens::contains)
                .count();

        return (double) matchingTokens / queryTokens.size();
    }
}
