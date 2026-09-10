package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LRUStrategyTest {

    @Test
    void evictsLeastRecentlyUsedFrame() {
        LRUStrategy strategy = new LRUStrategy();

        Frame frame0 = new Frame(0);
        Frame frame1 = new Frame(1);

        frame0.load(new Page(1), 0);
        frame1.load(new Page(2), 1);

        // Re-access frame0 later - it's now the MOST recently used, so
        // frame1 should be evicted instead, unlike FIFO.
        frame0.touch(5);

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 6);
        assertEquals(1, victim.getFrameId());
    }

    @Test
    void protectsFrequentlyReaccessedPage() {
        LRUStrategy strategy = new LRUStrategy();

        Frame frame0 = new Frame(0);
        Frame frame1 = new Frame(1);
        Frame frame2 = new Frame(2);

        frame0.load(new Page(1), 0);
        frame1.load(new Page(2), 1);
        frame2.load(new Page(3), 2);

        frame0.touch(3);
        frame0.touch(4);
        frame1.touch(5);

        // frame2 has the oldest lastAccessTime (2) - should be evicted
        Frame victim = strategy.selectVictim(List.of(frame0, frame1, frame2), 6);
        assertEquals(2, victim.getFrameId());
    }
}
