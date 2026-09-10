package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.MemoryReference;
import com.pagevault.core.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptimalStrategyTest {

    @Test
    void evictsPageUsedFurthestInFuture() {
        // Sequence: 1, 2, 3, 1, 2 - at index 2, resident pages are 1 and 2.
        // Page 1 is next used at index 3, page 2 at index 4 - page 2 should be evicted.
        List<MemoryReference> sequence = List.of(
                new MemoryReference(new Page(1), 0),
                new MemoryReference(new Page(2), 1),
                new MemoryReference(new Page(3), 2),
                new MemoryReference(new Page(1), 3),
                new MemoryReference(new Page(2), 4)
        );

        OptimalStrategy strategy = new OptimalStrategy(sequence);
        strategy.setCurrentIndex(2);

        Frame frame0 = new Frame(0);
        frame0.load(new Page(1), 0);
        Frame frame1 = new Frame(1);
        frame1.load(new Page(2), 1);

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 2);
        assertEquals(1, victim.getFrameId());
    }

    @Test
    void prefersEvictingPageNeverUsedAgain() {
        List<MemoryReference> sequence = List.of(
                new MemoryReference(new Page(1), 0),
                new MemoryReference(new Page(2), 1),
                new MemoryReference(new Page(1), 2) // page 2 is never referenced again after index 1
        );

        OptimalStrategy strategy = new OptimalStrategy(sequence);
        strategy.setCurrentIndex(1);

        Frame frame0 = new Frame(0);
        frame0.load(new Page(1), 0);
        Frame frame1 = new Frame(1);
        frame1.load(new Page(2), 1);

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 1);
        assertEquals(1, victim.getFrameId());
    }
}
