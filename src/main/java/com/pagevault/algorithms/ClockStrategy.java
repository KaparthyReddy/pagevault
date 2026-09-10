package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;

import java.util.List;

/**
 * The "second-chance" algorithm: frames are arranged in a circular buffer
 * with a sweeping hand. A frame's reference bit is set on access; when
 * looking for a victim, the hand sweeps forward, clearing reference bits
 * as it goes, and evicts the first frame it finds with a *clear* bit
 * (meaning it hasn't been touched since the hand last passed).
 *
 * This approximates LRU's behavior at much lower bookkeeping cost - real
 * operating systems use Clock (or variants of it) far more often than
 * true LRU, precisely because O(1)-per-access true LRU tracking is
 * expensive at OS scale.
 */
public class ClockStrategy implements PageReplacementStrategy {

    private int handPosition = 0;

    @Override
    public Frame selectVictim(List<Frame> frames, long currentTimestamp) {
        int size = frames.size();
        int scanned = 0;

        while (scanned < size * 2) { // at most two full sweeps: clear bits, then find one
            Frame current = frames.get(handPosition);
            handPosition = (handPosition + 1) % size;

            if (current.isFree()) {
                scanned++;
                continue;
            }

            if (!current.getReferenceBit()) {
                return current;
            }
            current.clearReferenceBit();
            scanned++;
        }

        throw new IllegalStateException("Clock sweep failed to find a victim - all frames free?");
    }

    @Override
    public void onAccess(Frame frame, Page page, long timestamp) {
        frame.touch(timestamp); // sets the reference bit, per Frame.touch()
    }

    @Override
    public String getName() { return "Clock"; }
}
