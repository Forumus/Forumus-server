package com.hcmus.forumus_backend.service;

import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Base64;

@Service
public class SummaryCacheService {

    public static class CachedSummary {
        private final String summary;
        private final String contentHash;
        private final long createdAt;
        private long lastAccessedAt;
        private int hitCount;

        public CachedSummary(String summary, String contentHash) {
            this.summary = summary;
            this.contentHash = contentHash;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = this.createdAt;
            this.hitCount = 0;
        }

        public String getSummary() {
            return summary;
        }

        public String getContentHash() {
            return contentHash;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public long getLastAccessedAt() {
            return lastAccessedAt;
        }

        public int getHitCount() {
            return hitCount;
        }

        public void recordAccess() {
            this.lastAccessedAt = System.currentTimeMillis();
            this.hitCount++;
        }

        public boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - createdAt > ttlMillis;
        }
    }

    public static class CacheStats {
        private long hits;
        private long misses;
        private long invalidations;
        private long evictions;

        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getInvalidations() { return invalidations; }
        public long getEvictions() { return evictions; }
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }

        public void recordHit() { hits++; }
        public void recordMiss() { misses++; }
        public void recordInvalidation() { invalidations++; }
        public void recordEviction() { evictions++; }
    }

    // Cache storage: postId -> CachedSummary
    private final ConcurrentHashMap<String, CachedSummary> cache = new ConcurrentHashMap<>();
    
    // Cache configuration
    private static final long DEFAULT_TTL_MILLIS = 24 * 60 * 60 * 1000; // 24 hours
    private static final int MAX_CACHE_SIZE = 10000; // Maximum entries
    
    // Cache statistics
    private final CacheStats stats = new CacheStats();

    public String computeContentHash(String title, String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = (title != null ? title : "") + "|" + (content != null ? content : "");
            byte[] hashBytes = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple hash
            return String.valueOf((title + "|" + content).hashCode());
        }
    }

    public CachedSummary get(String postId, String currentContentHash) {
        CachedSummary cached = cache.get(postId);
        
        if (cached == null) {
            stats.recordMiss();
            return null;
        }

        // Check if content has changed (invalidation)
        if (!cached.getContentHash().equals(currentContentHash)) {
            invalidate(postId);
            stats.recordInvalidation();
            stats.recordMiss();
            return null;
        }

        // Check TTL expiration
        if (cached.isExpired(DEFAULT_TTL_MILLIS)) {
            invalidate(postId);
            stats.recordEviction();
            stats.recordMiss();
            return null;
        }

        // Valid cache hit
        cached.recordAccess();
        stats.recordHit();
        return cached;
    }

    public void put(String postId, String summary, String contentHash) {
        // Evict old entries if cache is full
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictOldestEntries();
        }

        cache.put(postId, new CachedSummary(summary, contentHash));
    }

    public void invalidate(String postId) {
        cache.remove(postId);
    }

    public void clear() {
        cache.clear();
    }

    public int size() {
        return cache.size();
    }

    public CacheStats getStats() {
        return stats;
    }

    public boolean hasValidCache(String postId, String currentContentHash) {
        CachedSummary cached = cache.get(postId);
        if (cached == null) {
            return false;
        }
        return cached.getContentHash().equals(currentContentHash) && !cached.isExpired(DEFAULT_TTL_MILLIS);
    }

    private void evictOldestEntries() {
        int entriesToEvict = MAX_CACHE_SIZE / 10; // Evict 10%
        
        cache.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e1.getValue().getLastAccessedAt(), e2.getValue().getLastAccessedAt()))
            .limit(entriesToEvict)
            .forEach(entry -> {
                cache.remove(entry.getKey());
                stats.recordEviction();
            });
    }

    public String getCacheStatusSummary() {
        return String.format(
            "Cache Status: size=%d, hits=%d, misses=%d, hitRate=%.2f%%, invalidations=%d, evictions=%d",
            size(),
            stats.getHits(),
            stats.getMisses(),
            stats.getHitRate() * 100,
            stats.getInvalidations(),
            stats.getEvictions()
        );
    }
}
