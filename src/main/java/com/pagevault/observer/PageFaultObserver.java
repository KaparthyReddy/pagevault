package com.pagevault.observer;

import com.pagevault.core.PageFaultEvent;

/**
 * Observer interface: anything that wants to react to page faults
 * (logging, statistics, a future UI) implements this, without
 * MemoryManager needing to know what's listening or why.
 */
public interface PageFaultObserver {
    void onPageFault(PageFaultEvent event);
}
