package com.urlshortener.app.controller;

import com.urlshortener.app.dto.ShortenRequest;
import com.urlshortener.app.dto.ShortenResponse;
import com.urlshortener.app.service.RateLimiterService;
import com.urlshortener.app.service.UrlService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class UrlController {

    private final UrlService urlService;
    private final RateLimiterService rateLimiterService;

    public UrlController(UrlService urlService, RateLimiterService rateLimiterService) {
        this.urlService = urlService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody ShortenRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        if (!rateLimiterService.isAllowed(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded. Try after 1 minute.");
        }

        if (request.getOriginalUrl() == null || request.getOriginalUrl().isEmpty()) {
            return ResponseEntity.badRequest().body("URL cannot be empty");
        }

        ShortenResponse response = urlService.shortenUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        try {
            String originalUrl = urlService.getOriginalUrl(shortCode);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", originalUrl)
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL not found");
        }
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {
        try {
            Long clickCount = urlService.getClickCount(shortCode);
            return ResponseEntity.ok("Total clicks: " + clickCount);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("URL not found");
        }
    }
}
