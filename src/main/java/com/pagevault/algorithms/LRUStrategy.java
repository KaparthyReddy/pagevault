package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;

import java.util.List;

/**
 * Evicts the frame whose page was accessed least recently. Relies on
 * Frame's own lastAccessTime (updated on every hit, not just load), so
 * unlike FIFO this correctly protects frequently-reused pages from
 * eviction even if they were loaded long ago.
 *
 * Selection is O(n) over resident frames per fault, which is fine at
 * simulation scale; a production OS implementation would use a real
 * doubly-linked-list + hashmap for O(1) updates (the same technique used
 * elsewhere in this portfolio for LRU caches) - noted here as a known
 * scalability tradeoff rather than over-engineering a simulator.
 */
public class LRUStrategy implements PageReplacementStrategy {

    @Override
    public Frame selectVictim(List<Frame> frames, long currentTimestamp) {
        Frame oldest = null;
        for (Frame frame : frames) {
            if (frame.isFree()) continue;
            if (oldest == null || frame.getLastAccessTime() < oldest.getLastAccessTime()) {
                oldest = frame;
            }
        }
        if (oldest == null) {
            throw new IllegalStateException("No resident frame found to evict");
        }
        return oldest;
    }

    @Override
    public void onAccess(Frame frame, Page page, long timestamp) {
        frame.touch(timestamp); // Frame already tracks lastAccessTime itself
    }

    @Override
    public String getName() { return "LRU"; }
}
