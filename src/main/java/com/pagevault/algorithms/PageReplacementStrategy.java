package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;

import java.util.List;

/**
 * Strategy interface: given the current frame state, decide which frame's
 * page should be evicted to make room for a new page. Each implementation
 * also needs hooks for when a page is loaded/accessed, since several
 * algorithms (LRU, Clock) need to track access history that only the
 * algorithm itself cares about — MemoryManager stays algorithm-agnostic.
 */
public interface PageReplacementStrategy {

    /** Chooses which currently-occupied frame to evict. Called only when
     * all frames are full and a new page must be loaded. */
    Frame selectVictim(List<Frame> frames, long currentTimestamp);

    /** Called whenever a page is loaded into a frame (on fault) or
     * re-accessed (on hit), so algorithms can update their own bookkeeping. */
    void onAccess(Frame frame, Page page, long timestamp);

    String getName();
}
