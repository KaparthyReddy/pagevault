package com.pagevault.core;

/** One entry in a process's memory access trace: "at this logical step, access this page." */
public final class MemoryReference {
    private final Page page;
    private final long timestamp;

    public MemoryReference(Page page, long timestamp) {
        this.page = page;
        this.timestamp = timestamp;
    }

    public Page getPage() { return page; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() { return page + "@t" + timestamp; }
}
