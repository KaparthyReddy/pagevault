package com.pagevault.algorithms;

import com.pagevault.core.Frame;
import com.pagevault.core.Page;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClockStrategyTest {

    @Test
    void evictsFrameWithClearReferenceBit() {
        ClockStrategy strategy = new ClockStrategy();

        Frame frame0 = new Frame(0);
        frame0.load(new Page(1), 0);
        frame0.clearReferenceBit(); // simulate the bit having been cleared by a prior sweep

        Frame frame1 = new Frame(1);
        frame1.load(new Page(2), 1); // reference bit still set (true, from load())

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 2);
        assertEquals(0, victim.getFrameId());
    }

    @Test
    void givesSecondChanceToSetReferenceBitBeforeEvicting() {
        ClockStrategy strategy = new ClockStrategy();

        Frame frame0 = new Frame(0); // reference bit set (true) - gets a second chance
        frame0.load(new Page(1), 0);
        Frame frame1 = new Frame(1);
        frame1.load(new Page(2), 1);
        frame1.clearReferenceBit(); // this one should be evicted

        Frame victim = strategy.selectVictim(List.of(frame0, frame1), 2);

        assertEquals(1, victim.getFrameId());
        // frame0's bit should now be cleared, having been given its second chance
        assertFalse(frame0.getReferenceBit());
    }
}
