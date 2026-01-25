# AI Summary Caching Architecture

## Overview

This document describes the efficient caching system for AI-based text summarization implemented to avoid redundant API calls, minimize costs, and improve performance.

## Architecture Goals

1. **Avoid Redundant API Calls**: Cache summaries to prevent repeated AI generation requests
2. **Content-Aware Invalidation**: Automatically regenerate summaries when post content changes
3. **Cost Optimization**: Minimize Gemini API usage and quota consumption
4. **Multi-Layer Caching**: Both server-side and client-side caching for optimal performance
5. **Scalability**: Support multiple concurrent users efficiently

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT (Android App)                          │
├─────────────────────────────────────────────────────────────────────┤
│  ┌───────────────┐     ┌────────────────────────────────────────┐   │
│  │  HomeFragment │────▶│         SummaryCacheManager            │   │
│  │ PostDetailFrag│     │  (SharedPreferences-based cache)       │   │
│  └───────────────┘     │                                        │   │
│          │             │  - Local cache check (instant response)│   │
│          │             │  - Content hash validation             │   │
│          │             │  - TTL-based expiration (24h)          │   │
│          │             │  - LRU eviction (max 100 entries)      │   │
│          ▼             └────────────────────────────────────────┘   │
│  ┌───────────────┐                      │                           │
│  │PostRepository │◀─────────────────────┘                           │
│  │               │                                                   │
│  │ getPostSummary()                                                 │
│  └───────┬───────┘                                                   │
│          │ HTTP POST /api/posts/summarize                           │
└──────────┼──────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     SERVER (Spring Boot)                             │
├─────────────────────────────────────────────────────────────────────┤
│  ┌───────────────┐     ┌────────────────────────────────────────┐   │
│  │PostController │────▶│        SummaryCacheService             │   │
│  │               │     │   (ConcurrentHashMap-based cache)      │   │
│  │ /summarize    │     │                                        │   │
│  └───────────────┘     │  - Server-side cache check             │   │
│          │             │  - Content hash computation (SHA-256)  │   │
│          │             │  - TTL-based expiration (24h)          │   │
│          │             │  - LRU eviction (max 10,000 entries)   │   │
│          ▼             │  - Cache statistics tracking           │   │
│  ┌───────────────┐     └────────────────────────────────────────┘   │
│  │  PostService  │                      │                           │
│  │               │◀─────────────────────┘                           │
│  │summarizePost()│                                                   │
│  └───────┬───────┘                                                   │
│          │                                                           │
│          ▼ (Only on cache miss)                                     │
│  ┌───────────────┐                                                   │
│  │  Gemini API   │                                                   │
│  │ (gemini-2.5-  │                                                   │
│  │    flash)     │                                                   │
│  └───────────────┘                                                   │
└─────────────────────────────────────────────────────────────────────┘
```

## Cache Flow

### Request Flow

```
User clicks "Summary" button
        │
        ▼
┌──────────────────────────────────────┐
│ 1. Check LOCAL cache (instant)       │
│    - Valid & not expired?            │
│    - Content hash matches?           │
├──────────────────────────────────────┤
│     YES                              │     NO
│     ▼                                │     ▼
│ Return cached                        │ Call server API
│ summary                              │
└──────────────────────────────────────┘
                                              │
                                              ▼
                              ┌──────────────────────────────────────┐
                              │ 2. Check SERVER cache (fast)         │
                              │    - Valid & not expired?            │
                              │    - Content hash matches?           │
                              ├──────────────────────────────────────┤
                              │     YES                              │     NO
                              │     ▼                                │     ▼
                              │ Return cached                        │ Call Gemini API
                              │ (cached: true)                       │ (slow, costs quota)
                              └──────────────────────────────────────┘
                                                                            │
                                                                            ▼
                                              ┌──────────────────────────────────────┐
                                              │ 3. Store in server cache             │
                                              │    - Summary text                    │
                                              │    - Content hash                    │
                                              │    - Generated timestamp             │
                                              └──────────────────────────────────────┘
                                                                            │
                                                                            ▼
                              ┌──────────────────────────────────────┐
                              │ 4. Response to client                │
                              │    - summary                         │
                              │    - cached: false/true              │
                              │    - contentHash                     │
                              │    - generatedAt                     │
                              │    - expiresAt                       │
                              └──────────────────────────────────────┘
                                              │
                                              ▼
                              ┌──────────────────────────────────────┐
                              │ 5. Store in LOCAL cache              │
                              │    - For next request                │
                              └──────────────────────────────────────┘
```

## Content Hash Invalidation

The system uses SHA-256 content hashing to detect when post content changes:

```java
// Server-side hash computation
public String computeContentHash(String title, String content) {
    String combined = (title != null ? title : "") + "|" + (content != null ? content : "");
    byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(combined.getBytes());
    return Base64.getEncoder().encodeToString(hashBytes);
}
```

When content changes:
1. New request computes new content hash
2. Hash doesn't match cached entry
3. Old cache entry is invalidated
4. New summary is generated and cached

## API Response Structure

```json
{
    "success": true,
    "summary": "This post discusses the implementation of...",
    "errorMessage": null,
    "cached": true,
    "contentHash": "a3f2b8c9d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1",
    "generatedAt": 1706194800000,
    "expiresAt": 1706281200000
}
```

## Cache Configuration

### Server-Side (SummaryCacheService)

| Parameter | Value | Description |
|-----------|-------|-------------|
| TTL | 24 hours | Cache entry lifetime |
| Max Size | 10,000 entries | Maximum cached summaries |
| Eviction Policy | LRU (10%) | Removes oldest 10% when full |
| Hash Algorithm | SHA-256 | Content change detection |

### Client-Side (SummaryCacheManager)

| Parameter | Value | Description |
|-----------|-------|-------------|
| TTL | 24 hours | Cache entry lifetime |
| Max Size | 100 entries | Maximum cached summaries |
| Eviction Policy | LRU (20%) | Removes oldest 20% when full |
| Storage | SharedPreferences | Persistent local storage |

## Performance Benefits

### Without Caching
- Every summary request → Gemini API call
- Response time: 3-10 seconds
- Cost: Full API quota per request
- No offline support

### With Caching
- Repeated requests → Instant response
- Response time: <100ms (local), <500ms (server cache)
- Cost: Only first request uses quota
- Offline support for cached summaries

## Cache Statistics

Both cache services track statistics:

```
Cache Status: size=150, hits=1234, misses=56, hitRate=95.67%, invalidations=12, evictions=0
```

- **Hits**: Requests served from cache
- **Misses**: Requests requiring new generation
- **Hit Rate**: Percentage of cached responses
- **Invalidations**: Entries removed due to content change
- **Evictions**: Entries removed due to size limit

## Server Files

| File | Description |
|------|-------------|
| `SummaryCacheService.java` | In-memory cache service with ConcurrentHashMap |
| `PostService.java` | Updated to use cache before calling Gemini |
| `PostSummaryResponse.java` | DTO with cache metadata fields |

## Testing the Cache

### Via cURL

```bash
# First request - generates new summary
curl -X POST http://localhost:3000/api/posts/summarize \
  -H "Content-Type: application/json" \
  -d '{"postId": "POST_20260125_120000_1234"}'

# Check server logs for "Cache MISS"

# Second request - returns cached
curl -X POST http://localhost:3000/api/posts/summarize \
  -H "Content-Type: application/json" \
  -d '{"postId": "POST_20260125_120000_1234"}'

# Check server logs for "Cache HIT"
# Response will have "cached": true
```

### Expected Logs

```
Summary request for Post ID: POST_20260125_120000_1234
Cache MISS for post POST_20260125_120000_1234 - generating new summary
Cache status: Cache Status: size=0, hits=0, misses=0, hitRate=0.00%, invalidations=0, evictions=0
Summarizing post - Title: Sample Post
Summary generated and cached: This post discusses...
New cache status: Cache Status: size=1, hits=0, misses=1, hitRate=0.00%, invalidations=0, evictions=0

# Second request
Summary request for Post ID: POST_20260125_120000_1234
Cache HIT for post POST_20260125_120000_1234 (hitCount: 1)
```

## Future Improvements

1. **Redis Cache**: Replace in-memory cache with Redis for distributed caching
2. **Pre-warming**: Generate summaries for popular posts proactively
3. **Compression**: Compress cached summaries to save storage
4. **Cache Sync**: Sync cache invalidation across server instances
5. **Smart TTL**: Adjust TTL based on post update frequency
