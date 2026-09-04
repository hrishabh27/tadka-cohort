package com.tadka.api.infrastructure.caching;

import java.time.Duration;
import java.util.function.Supplier;

public interface CacheService {
    <T> T getOrSet(String key, Duration ttl, Supplier<T> factory, Class<T> clazz);
    void evict(String key);
}
