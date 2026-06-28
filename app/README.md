# URL Shortener

Built this as a backend project to understand how services like bit.ly work internally.

## What it does
Takes a long URL and returns a short one. When someone visits the short link, they get redirected to the original URL. Also tracks how many times each link was clicked.

## Tech used
- Java + Spring Boot
- MySQL (stores URLs)
- Redis (caching + rate limiting)

## How to run locally
1. Clone the repo
2. Copy `application-example.properties` to `application.properties` and add your credentials
3. Make sure MySQL and Redis are running
4. Run the Spring Boot app

## API
- `POST /api/shorten` — pass a URL, get a short code back
- `GET /{shortCode}` — redirects to original URL
- `GET /api/stats/{shortCode}` — returns click count