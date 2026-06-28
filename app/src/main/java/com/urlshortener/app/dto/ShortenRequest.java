package com.urlshortener.app.dto;

import lombok.Data;

@Data
public class ShortenRequest {
    private String originalUrl;
}
