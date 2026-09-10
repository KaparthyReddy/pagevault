package com.pagevault;

import com.pagevault.algorithms.*;
import com.pagevault.core.MemoryReference;
import com.pagevault.engine.MemoryManager;
import com.pagevault.io.ReferenceStringGenerator;
import com.pagevault.observer.StatisticsCollector;

import java.util.List;
import java.util.function.Supplier;

/**
 * Demo runner: generates three different workload patterns (random,
 * sequential, locality-of-reference) and runs all four replacement
 * algorithms against each, printing a comparison table of fault counts
 * and hit ratios - the point being to actually show *why* algorithm
 * choice matters, not just that each one runs without crashing.
 */
public class Main {

    private static final int FRAME_COUNT = 4;
    private static final int ADDRESS_SPACE_SIZE = 20;
    private static final int SEQUENCE_LENGTH = 200;

    public static void main(String[] args) {
        ReferenceStringGenerator generator = new ReferenceStringGenerator(42);

        List<MemoryReference> randomWorkload = generator.generateRandom(SEQUENCE_LENGTH, ADDRESS_SPACE_SIZE);
        List<MemoryReference> sequentialWorkload = generator.generateSequential(SEQUENCE_LENGTH, ADDRESS_SPACE_SIZE);
        List<MemoryReference> localityWorkload = generator.generateLocalityPattern(
                SEQUENCE_LENGTH, ADDRESS_SPACE_SIZE, /* workingSetSize */ 6, /* burstLength */ 15
        );

        runComparison("Random Access", randomWorkload);
        runComparison("Sequential Access", sequentialWorkload);
        runComparison("Locality-of-Reference (bursty)", localityWorkload);
    }

    private static void runComparison(String workloadName, List<MemoryReference> workload) {
        System.out.println("\n=== Workload: " + workloadName + " (" + workload.size() + " references, "
                + FRAME_COUNT + " frames) ===");
        System.out.printf("%-10s %10s %10s %12s%n", "Algorithm", "Faults", "Hits", "Hit Ratio");
        System.out.println("-".repeat(46));

        runOne("FIFO", () -> new FIFOStrategy(), workload);
        runOne("LRU", () -> new LRUStrategy(), workload);
        runOne("Clock", () -> new ClockStrategy(), workload);

        // Optimal needs the full sequence up front, so it's constructed
        // differently from the other three - still plugs into the same
        // MemoryManager via the same interface.
        runOptimal(workload);
    }

    private static void runOne(String label, Supplier<PageReplacementStrategy> strategySupplier,
                                List<MemoryReference> workload) {
        PageReplacementStrategy strategy = strategySupplier.get();
        MemoryManager manager = new MemoryManager(FRAME_COUNT, strategy);
        StatisticsCollector stats = new StatisticsCollector();
        manager.addObserver(stats);

        for (MemoryReference reference : workload) {
            stats.recordReference();
            manager.accessPage(reference);
        }

        printRow(label, stats);
    }

    private static void runOptimal(List<MemoryReference> workload) {
        OptimalStrategy strategy = new OptimalStrategy(workload);
        MemoryManager manager = new MemoryManager(FRAME_COUNT, strategy);
        StatisticsCollector stats = new StatisticsCollector();
        manager.addObserver(stats);

        for (int i = 0; i < workload.size(); i++) {
            strategy.setCurrentIndex(i);
            stats.recordReference();
            manager.accessPage(workload.get(i));
        }

        printRow("Optimal", stats);
    }

    private static void printRow(String label, StatisticsCollector stats) {
        System.out.printf("%-10s %10d %10d %11.1f%%%n",
                label, stats.getFaultCount(), stats.getHitCount(), stats.getHitRatio() * 100);
    }
}
