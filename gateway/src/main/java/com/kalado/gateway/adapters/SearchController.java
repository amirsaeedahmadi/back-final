package com.kalado.gateway.adapters;

import com.kalado.common.dto.ProductDto;
import com.kalado.common.feign.search.SearchApi;
import com.kalado.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchApi searchApi;

    @GetMapping("/products")
    public Page<ProductDto> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String timeFilter,
            @RequestParam(required = false, defaultValue = "date") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ProductDto> response = searchApi.searchProducts(
                keyword, minPrice, maxPrice, timeFilter,
                sortBy, sortOrder, page, size
        );
        return response.toPage();
    }
}