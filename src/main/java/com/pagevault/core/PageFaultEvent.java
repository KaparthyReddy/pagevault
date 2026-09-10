package com.pagevault.core;

/** Emitted by MemoryManager whenever a referenced page isn't resident. */
public final class PageFaultEvent {
    private final Page requestedPage;
    private final Page evictedPage; // null if a free frame was available, no eviction needed
    private final long timestamp;

    public PageFaultEvent(Page requestedPage, Page evictedPage, long timestamp) {
        this.requestedPage = requestedPage;
        this.evictedPage = evictedPage;
        this.timestamp = timestamp;
    }

    public Page getRequestedPage() { return requestedPage; }
    public Page getEvictedPage() { return evictedPage; }
    public boolean requiredEviction() { return evictedPage != null; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return requiredEviction()
                ? String.format("FAULT: %s in, %s evicted @t%d", requestedPage, evictedPage, timestamp)
                : String.format("FAULT: %s loaded into free frame @t%d", requestedPage, timestamp);
    }
}
