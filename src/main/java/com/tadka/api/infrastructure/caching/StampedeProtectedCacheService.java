package com.tadka.api.infrastructure.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class StampedeProtectedCacheService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(StampedeProtectedCacheService.class);

    private record CacheEntry(Object value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSet(String key, Duration ttl, Supplier<T> factory, Class<T> clazz) {
        CacheEntry entry = store.get(key);
        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for key: {}", key);
            return (T) entry.value();
        }

        // Cache miss or expired: acquire single-flight stampede lock for this key
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check after acquiring lock in case another thread populated it
            entry = store.get(key);
            if (entry != null && !entry.isExpired()) {
                log.debug("Cache hit (post-lock) for key: {}", key);
                return (T) entry.value();
            }

            log.debug("Cache miss - calculating value for key: {}", key);
            T value = factory.get();
            if (value != null) {
                store.put(key, new CacheEntry(value, Instant.now().plus(ttl)));
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void evict(String key) {
        log.debug("Cache evict for key: {}", key);
        store.remove(key);
    }
}
