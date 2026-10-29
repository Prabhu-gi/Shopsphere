package com.shopsphere.common.config;

import com.shopsphere.common.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final long capacity;
    private final long tokens;
    private final long refillSeconds;

    public RateLimiterService(
            @Value("${app.rate-limit.auth.capacity:15}") long capacity,
            @Value("${app.rate-limit.auth.tokens:15}") long tokens,
            @Value("${app.rate-limit.auth.refill-seconds:60}") long refillSeconds) {
        this.capacity = capacity;
        this.tokens = tokens;
        this.refillSeconds = refillSeconds;
    }

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, this::newBucket);
    }

    public void checkRateLimit(String key) {
        Bucket bucket = resolveBucket(key);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many requests. Please try again in a few moments.");
        }
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(tokens, Duration.ofSeconds(refillSeconds))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}

