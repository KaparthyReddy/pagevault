package com.pagevault.core;

/**
 * A physical frame slot in memory. Tracks which page currently occupies it
 * (if any) and metadata some algorithms need: load time (FIFO), last-access
 * time (LRU), and a reference bit (Clock).
 */
public class Frame {
    private final int frameId;
    private Page residentPage;
    private long loadTime;
    private long lastAccessTime;
    private boolean referenceBit;

    public Frame(int frameId) {
        this.frameId = frameId;
    }

    public int getFrameId() { return frameId; }

    public Page getResidentPage() { return residentPage; }

    public boolean isFree() { return residentPage == null; }

    public void load(Page page, long timestamp) {
        this.residentPage = page;
        this.loadTime = timestamp;
        this.lastAccessTime = timestamp;
        this.referenceBit = true;
    }

    public void touch(long timestamp) {
        this.lastAccessTime = timestamp;
        this.referenceBit = true;
    }

    public long getLoadTime() { return loadTime; }
    public long getLastAccessTime() { return lastAccessTime; }

    public boolean getReferenceBit() { return referenceBit; }
    public void clearReferenceBit() { this.referenceBit = false; }

    @Override
    public String toString() {
        return String.format("Frame[%d]=%s", frameId, residentPage == null ? "empty" : residentPage);
    }
}
