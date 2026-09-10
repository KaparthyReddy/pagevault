package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FIFOStrategyTest {

    @Test
    void evictsEarliestLoadedFrame() {
        FIFOStrategy strategy = new FIFOStrategy();

        Frame frame0 = new Frame(0);
        Frame frame1 = new Frame(1);
        Frame frame2 = new Frame(2);

        frame0.load(new Page(1), 0);
        strategy.onAccess(frame0, new Page(1), 0);

        frame1.load(new Page(2), 1);
        strategy.onAccess(frame1, new Page(2), 1);

        frame2.load(new Page(3), 2);
        strategy.onAccess(frame2, new Page(3), 2);

        Frame victim = strategy.selectVictim(List.of(frame0, frame1, frame2), 3);
        assertEquals(0, victim.getFrameId());
    }

    @Test
    void ignoresReaccessWhenChoosingVictim() {
        FIFOStrategy strategy = new FIFOStrategy();

        Frame frame0 = new Frame(0);
        Frame frame1 = new Frame(1);

        frame0.load(new Page(1), 0);
        strategy.onAccess(frame0, new Page(1), 0);

        frame1.load(new Page(2), 1);
        strategy.onAccess(frame1, new Page(2), 1);

        // Re-access frame0's page - FIFO should NOT protect it, since it
        // only cares about load order, not recency of use.
        frame0.touch(2);
        strategy.onAccess(frame0, new Page(1), 2); // timestamp != loadTime, so not re-enqueued

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 3);
        assertEquals(0, victim.getFrameId());
    }
}
