package com.urlshortener.app.service;

import com.urlshortener.app.dto.ShortenRequest;
import com.urlshortener.app.dto.ShortenResponse;
import com.urlshortener.app.model.Url;
import com.urlshortener.app.repository.UrlRepository;
import com.urlshortener.app.util.Base62Encoder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BASE_URL = "http://localhost:8080/";
    private static final long CACHE_TTL = 24;

    public UrlService(UrlRepository urlRepository, Base62Encoder base62Encoder,
                      RedisTemplate<String, String> redisTemplate) {
        this.urlRepository = urlRepository;
        this.base62Encoder = base62Encoder;
        this.redisTemplate = redisTemplate;
    }

    public ShortenResponse shortenUrl(ShortenRequest request) {
        return urlRepository.findByOriginalUrl(request.getOriginalUrl())
                .map(existingUrl -> {
                    ShortenResponse response = new ShortenResponse();
                    response.setShortUrl(BASE_URL + existingUrl.getShortCode());
                    response.setShortCode(existingUrl.getShortCode());
                    response.setClickCount(existingUrl.getClickCount());
                    return response;
                })
                .orElseGet(() -> {
                    Url url = new Url();
                    url.setOriginalUrl(request.getOriginalUrl());
                    url.setShortCode("temp");
                    Url saved = urlRepository.save(url);

                    String shortCode = base62Encoder.encode(saved.getId());
                    saved.setShortCode(shortCode);
                    urlRepository.save(saved);

                    redisTemplate.opsForValue().set(
                            "url:" + shortCode,
                            request.getOriginalUrl(),
                            CACHE_TTL,
                            TimeUnit.HOURS
                    );

                    ShortenResponse response = new ShortenResponse();
                    response.setShortUrl(BASE_URL + shortCode);
                    response.setShortCode(shortCode);
                    response.setClickCount(0L);
                    return response;
                });
    }

    public String getOriginalUrl(String shortCode) {
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);
        if (cachedUrl != null) {
            redisTemplate.opsForValue().increment("clicks:" + shortCode);
            return cachedUrl;
        }

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        redisTemplate.opsForValue().set(
                "url:" + shortCode,
                url.getOriginalUrl(),
                CACHE_TTL,
                TimeUnit.HOURS
        );

        url.setClickCount(url.getClickCount() + 1);
        urlRepository.save(url);

        return url.getOriginalUrl();
    }

    public Long getClickCount(String shortCode) {
        String redisCount = redisTemplate.opsForValue().get("clicks:" + shortCode);
        if (redisCount != null) {
            return Long.parseLong(redisCount);
        }
        return urlRepository.findByShortCode(shortCode)
                .map(Url::getClickCount)
                .orElseThrow(() -> new RuntimeException("URL not found"));
    }
}
