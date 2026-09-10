package com.pagevault.observer;

import com.pagevault.core.PageFaultEvent;

/**
 * Tracks aggregate fault/hit counts without storing every individual
 * event - a lighter-weight observer than FaultLogger, useful when you
 * only care about the final numbers (hit ratio, fault count) rather than
 * the full replay history.
 */
public class StatisticsCollector implements PageFaultObserver {

    private int faultCount = 0;
    private int totalReferences = 0;

    @Override
    public void onPageFault(PageFaultEvent event) {
        faultCount++;
    }

    /** Called by MemoryManager on every reference, hit or fault, to track the denominator. */
    public void recordReference() {
        totalReferences++;
    }

    public int getFaultCount() { return faultCount; }
    public int getHitCount() { return totalReferences - faultCount; }
    public int getTotalReferences() { return totalReferences; }

    public double getHitRatio() {
        return totalReferences == 0 ? 0.0 : (double) getHitCount() / totalReferences;
    }

    public double getFaultRatio() {
        return totalReferences == 0 ? 0.0 : (double) faultCount / totalReferences;
    }

    public void reset() {
        faultCount = 0;
        totalReferences = 0;
    }
}
