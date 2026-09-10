package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.MemoryReference;
import com.pagevault.core.Page;

import java.util.List;

/**
 * The theoretical best-case algorithm (Belady's optimal): evicts whichever
 * resident page will be used furthest in the future, or never again. This
 * requires knowing the entire future reference stream in advance, which is
 * impossible for a real OS to know - Optimal exists purely as a baseline
 * to measure how close a *real* (causal) algorithm like LRU or Clock gets
 * to the best achievable fault rate.
 *
 * Unlike the other strategies, this one needs the full reference sequence
 * up front rather than learning incrementally, so it's constructed with it
 * directly instead of building state via onAccess().
 */
public class OptimalStrategy implements PageReplacementStrategy {

    private final List<MemoryReference> fullSequence;
    private int currentIndex;

    public OptimalStrategy(List<MemoryReference> fullSequence) {
        this.fullSequence = fullSequence;
    }

    /** MemoryManager calls this before each reference so Optimal knows
     * "where we are" in the sequence when deciding a victim. */
    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    @Override
    public Frame selectVictim(List<Frame> frames, long currentTimestamp) {
        Frame victim = null;
        int farthestNextUse = -1;

        for (Frame frame : frames) {
            if (frame.isFree()) continue;

            int nextUse = findNextUseIndex(frame.getResidentPage());
            if (nextUse == -1) {
                return frame; // never used again - the ideal, unambiguous choice
            }
            if (nextUse > farthestNextUse) {
                farthestNextUse = nextUse;
                victim = frame;
            }
        }

        if (victim == null) {
            throw new IllegalStateException("No resident frame found to evict");
        }
        return victim;
    }

    private int findNextUseIndex(Page page) {
        for (int i = currentIndex + 1; i < fullSequence.size(); i++) {
            if (fullSequence.get(i).getPage().equals(page)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onAccess(Frame frame, Page page, long timestamp) {
        // No bookkeeping needed - Optimal decides purely by looking ahead
        // at the known future sequence, not by tracking past access history.
    }

    @Override
    public String getName() { return "Optimal"; }
}
