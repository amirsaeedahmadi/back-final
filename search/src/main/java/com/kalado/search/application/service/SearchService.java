package com.kalado.search.application.service;

import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.infrastructure.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private static final DateTimeFormatter ES_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public Page<ProductDocument> searchProducts(
            String keyword,
            Double minPrice,
            Double maxPrice,
            String timeFilter,
            String sortBy,
            SortOrder sortOrder,
            Pageable pageable
    ) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword)
                    .field("title", 2.0f)
                    .field("title.english")
                    .field("description")
                    .field("description.english")
                    .field("brand")
                    .type("best_fields")
                    .fuzziness("AUTO"));
        }

        if (minPrice != null || maxPrice != null) {
            boolQuery.must(QueryBuilders.rangeQuery("price.amount")
                    .from(minPrice != null ? minPrice : 0)
                    .to(maxPrice));
        }

        if (timeFilter != null) {
            LocalDateTime fromDate = null;
            LocalDateTime now = LocalDateTime.now();

            switch (timeFilter) {
                case "1D" -> fromDate = now.minusDays(1);
                case "1W" -> fromDate = now.minusWeeks(1);
                case "1M" -> fromDate = now.minusMonths(1);
                default -> log.warn("Invalid time filter: {}", timeFilter);
            }

            if (fromDate != null) {
                String fromDateStr = fromDate.atZone(ZoneOffset.UTC).format(ES_DATE_FORMAT);
                String toDateStr = now.atZone(ZoneOffset.UTC).format(ES_DATE_FORMAT);

                boolQuery.must(QueryBuilders.rangeQuery("createdAt")
                        .from(fromDateStr)
                        .to(toDateStr));
            }
        }

        boolQuery.mustNot(QueryBuilders.termQuery("status", "DELETED"));

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable);

        if (sortBy != null && sortOrder != null) {
            switch (sortBy) {
                case "price" -> searchQueryBuilder.withSort(SortBuilders.fieldSort("price.amount").order(sortOrder));
                case "date" -> searchQueryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(sortOrder));
                default -> searchQueryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC));
            }
        } else {
            searchQueryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC));
        }

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                searchQueryBuilder.build(),
                ProductDocument.class
        );

        List<ProductDocument> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(
                products,
                pageable,
                searchHits.getTotalHits()
        );
    }

    public void indexProduct(ProductDocument product) {
        productSearchRepository.save(product);
    }

    public void deleteProduct(String id) {
        productSearchRepository.deleteById(id);
    }
}