package com.urlshortener.app.dto;

import lombok.Data;

@Data
public class ShortenResponse {
    private String shortUrl;
    private String shortCode;
    private Long clickCount;
}
