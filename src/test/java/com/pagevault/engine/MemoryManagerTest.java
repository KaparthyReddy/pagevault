package com.pagevault.engine;

import com.pagevault.algorithms.FIFOStrategy;
import com.pagevault.core.MemoryReference;
import com.pagevault.core.Page;
import com.pagevault.core.PageFaultEvent;
import com.pagevault.observer.FaultLogger;
import com.pagevault.observer.StatisticsCollector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemoryManagerTest {

    @Test
    void firstAccessesToEmptyFramesAreAllFaults() {
        MemoryManager manager = new MemoryManager(3, new FIFOStrategy());

        boolean hit1 = manager.accessPage(new MemoryReference(new Page(1), 0));
        boolean hit2 = manager.accessPage(new MemoryReference(new Page(2), 1));
        boolean hit3 = manager.accessPage(new MemoryReference(new Page(3), 2));

        assertFalse(hit1);
        assertFalse(hit2);
        assertFalse(hit3);
    }

    @Test
    void reaccessingResidentPageIsAHit() {
        MemoryManager manager = new MemoryManager(3, new FIFOStrategy());

        manager.accessPage(new MemoryReference(new Page(1), 0));
        boolean hit = manager.accessPage(new MemoryReference(new Page(1), 1));

        assertTrue(hit);
    }

    @Test
    void evictionOccursOnlyWhenFramesAreFull() {
        MemoryManager manager = new MemoryManager(2, new FIFOStrategy());
        FaultLogger logger = new FaultLogger();
        manager.addObserver(logger);

        manager.accessPage(new MemoryReference(new Page(1), 0));
        manager.accessPage(new MemoryReference(new Page(2), 1));
        manager.accessPage(new MemoryReference(new Page(3), 2)); // triggers eviction

        List<PageFaultEvent> history = logger.getHistory();
        assertEquals(3, history.size());
        assertFalse(history.get(0).requiredEviction());
        assertFalse(history.get(1).requiredEviction());
        assertTrue(history.get(2).requiredEviction());
        assertEquals(new Page(1), history.get(2).getEvictedPage());
    }

    @Test
    void statisticsCollectorTracksHitsAndFaultsCorrectly() {
        MemoryManager manager = new MemoryManager(2, new FIFOStrategy());
        StatisticsCollector stats = new StatisticsCollector();
        manager.addObserver(stats);

        List<MemoryReference> sequence = List.of(
                new MemoryReference(new Page(1), 0),
                new MemoryReference(new Page(2), 1),
                new MemoryReference(new Page(1), 2), // hit
                new MemoryReference(new Page(3), 3)  // fault, evicts page 2 (FIFO)
        );

        for (MemoryReference ref : sequence) {
            stats.recordReference();
            manager.accessPage(ref);
        }

        assertEquals(4, stats.getTotalReferences());
        assertEquals(3, stats.getFaultCount());
        assertEquals(1, stats.getHitCount());
        assertEquals(0.25, stats.getHitRatio(), 0.001);
    }

    @Test
    void runSequenceReturnsCorrectFaultCount() {
        MemoryManager manager = new MemoryManager(2, new FIFOStrategy());

        List<MemoryReference> sequence = List.of(
                new MemoryReference(new Page(1), 0),
                new MemoryReference(new Page(2), 1),
                new MemoryReference(new Page(1), 2),
                new MemoryReference(new Page(3), 3)
        );

        int faults = manager.runSequence(sequence);
        assertEquals(3, faults);
    }
}
