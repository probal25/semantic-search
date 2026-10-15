package basic.semanticsearchengine.controller;

import basic.semanticsearchengine.domain.model.SearchResult;
import basic.semanticsearchengine.domain.request.SearchRequest;
import basic.semanticsearchengine.domain.response.SearchResponse;
import basic.semanticsearchengine.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public List<SearchResponse> search(@Valid @RequestBody SearchRequest request)
            throws IOException, InterruptedException, SQLException {

        List<SearchResult> results =
                searchService.search(request.getQuery(), request.getLimit());

        return results.stream()
                .map(result -> new SearchResponse(
                        result.getDocument().getText(),
                        result.getScore()))
                .toList();
    }
}