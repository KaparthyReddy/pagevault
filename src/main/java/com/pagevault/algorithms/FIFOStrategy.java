package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Evicts whichever resident page was loaded longest ago, regardless of how
 * recently it was accessed. Tracked with a plain FIFO queue of frame IDs -
 * O(1) to enqueue on load, O(1) to dequeue the victim.
 */
public class FIFOStrategy implements PageReplacementStrategy {

    private final Queue<Integer> loadOrder = new LinkedList<>();

    @Override
    public Frame selectVictim(List<Frame> frames, long currentTimestamp) {
        while (!loadOrder.isEmpty()) {
            int candidateFrameId = loadOrder.poll();
            for (Frame frame : frames) {
                if (frame.getFrameId() == candidateFrameId && !frame.isFree()) {
                    return frame;
                }
            }
        }
        throw new IllegalStateException("No victim found - frame tracking is out of sync");
    }

    @Override
    public void onAccess(Frame frame, Page page, long timestamp) {
        // Only track load order on first load into this frame, not on repeat
        // hits - FIFO deliberately ignores recency of access.
        if (frame.getLoadTime() == timestamp) {
            loadOrder.add(frame.getFrameId());
        }
    }

    @Override
    public String getName() { return "FIFO"; }
}
