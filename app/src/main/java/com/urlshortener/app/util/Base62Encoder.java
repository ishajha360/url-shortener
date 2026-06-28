package com.urlshortener.app.util;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String encode(Long id) {
        StringBuilder shortCode = new StringBuilder();

        while (id > 0) {
            shortCode.append(CHARACTERS.charAt((int) (id % 62)));
            id = id / 62;
        }

        return shortCode.reverse().toString();
    }
}
