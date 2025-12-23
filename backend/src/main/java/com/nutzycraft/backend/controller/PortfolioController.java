package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.PortfolioItem;
import com.nutzycraft.backend.service.ContentfulPortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final ContentfulPortfolioService portfolioService;

    @Autowired
    public PortfolioController(ContentfulPortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<List<PortfolioItem>> getPortfolioItems() {
        return ResponseEntity.ok(portfolioService.getPortfolioItems());
    }
}
