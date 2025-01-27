package com.kalado.search.adapters.controller;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.feign.search.SearchApi;
import com.kalado.common.response.PageResponse;
import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.mapper.ProductSearchMapper;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController implements SearchApi {
    private final SearchService searchService;
    private final ProductSearchMapper mapper;

    @Override
    public PageResponse<ProductDto> searchProducts(
            String keyword,
            Double minPrice,
            Double maxPrice,
            String timeFilter,
            String sortBy,
            String sortOrder,
            int page,
            int size
    ) {
        return PageResponse.from(
                mapper.toProductDtoPage(
                        searchService.searchProducts(
                                keyword,
                                minPrice,
                                maxPrice,
                                timeFilter,
                                sortBy,
                                SortOrder.valueOf(sortOrder),
                                PageRequest.of(page, size)
                        )
                )
        );
    }
}