package com.pagevault.io;

import com.pagevault.core.MemoryReference;
import com.pagevault.core.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates synthetic memory reference streams representing different
 * real-world access patterns, so the demo can show how each replacement
 * algorithm behaves under genuinely different workloads rather than just
 * one arbitrary sequence.
 */
public class ReferenceStringGenerator {

    private final Random random;

    public ReferenceStringGenerator(long seed) {
        this.random = new Random(seed);
    }

    /** Uniformly random page accesses across the full address space - the
     * worst case for locality-exploiting algorithms like LRU/Clock, since
     * there's no pattern to exploit. */
    public List<MemoryReference> generateRandom(int length, int addressSpaceSize) {
        List<MemoryReference> references = new ArrayList<>();
        for (int t = 0; t < length; t++) {
            int pageNumber = random.nextInt(addressSpaceSize);
            references.add(new MemoryReference(new Page(pageNumber), t));
        }
        return references;
    }

    /** Strictly increasing page access, wrapping around - simulates a
     * process scanning through memory linearly (e.g. sequential array
     * traversal). FIFO and LRU perform identically here, since load order
     * and access order are the same thing under pure sequential access. */
    public List<MemoryReference> generateSequential(int length, int addressSpaceSize) {
        List<MemoryReference> references = new ArrayList<>();
        for (int t = 0; t < length; t++) {
            int pageNumber = t % addressSpaceSize;
            references.add(new MemoryReference(new Page(pageNumber), t));
        }
        return references;
    }

    /** Locality-of-reference pattern: repeatedly accesses a small "working
     * set" of nearby pages for a while before jumping to a new working
     * set - this is what real programs actually look like (loops
     * operating on a local region of data), and is exactly the pattern
     * LRU/Clock are designed to exploit well. */
    public List<MemoryReference> generateLocalityPattern(int length, int addressSpaceSize,
                                                            int workingSetSize, int burstLength) {
        List<MemoryReference> references = new ArrayList<>();
        int t = 0;

        while (t < length) {
            int workingSetStart = random.nextInt(Math.max(1, addressSpaceSize - workingSetSize));
            int burstEnd = Math.min(t + burstLength, length);

            while (t < burstEnd) {
                int offset = random.nextInt(workingSetSize);
                int pageNumber = workingSetStart + offset;
                references.add(new MemoryReference(new Page(pageNumber), t));
                t++;
            }
        }

        return references;
    }
}
