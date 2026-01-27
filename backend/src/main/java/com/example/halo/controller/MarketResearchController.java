package com.example.halo.controller;

import com.example.halo.dto.ProductHuntPostDto;
import com.example.halo.service.ProductHuntService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/market-research")
public class MarketResearchController {

    private final ProductHuntService productHuntService;

    public MarketResearchController(ProductHuntService productHuntService) {
        this.productHuntService = productHuntService;
    }

    @GetMapping("/search")
    public Mono<List<ProductHuntPostDto>> searchProductHuntPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return productHuntService.searchPosts(query, limit);
    }
}
