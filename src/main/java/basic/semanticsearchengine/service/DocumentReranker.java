package basic.semanticsearchengine.service;

import basic.semanticsearchengine.domain.model.SearchResult;

import java.util.List;

public interface DocumentReranker {

    List<SearchResult> rerank(
            String query,
            List<SearchResult> candidates);
}