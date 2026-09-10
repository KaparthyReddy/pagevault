package com.pagevault.engine;

import com.pagevault.algorithms.PageReplacementStrategy;
import com.pagevault.core.Frame;
import com.pagevault.core.MemoryReference;
import com.pagevault.core.Page;
import com.pagevault.core.PageFaultEvent;
import com.pagevault.observer.PageFaultObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simulates a fixed-size physical memory (a set of frames) servicing a
 * process's memory reference stream, using a pluggable replacement
 * strategy to decide evictions. This class deliberately knows nothing
 * about *how* any given strategy picks a victim - that's the entire
 * point of the Strategy pattern here.
 */
public class MemoryManager {

    private final List<Frame> frames;
    private final PageReplacementStrategy strategy;
    private final List<PageFaultObserver> observers = new CopyOnWriteArrayList<>();

    public MemoryManager(int frameCount, PageReplacementStrategy strategy) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("Frame count must be positive");
        }
        this.strategy = strategy;
        this.frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(new Frame(i));
        }
    }

    public void addObserver(PageFaultObserver observer) {
        observers.add(observer);
    }

    /** Processes one memory reference: a hit if the page is resident, a fault otherwise. */
    public boolean accessPage(MemoryReference reference) {
        Page page = reference.getPage();
        long timestamp = reference.getTimestamp();

        Frame residentFrame = findFrameHolding(page);
        if (residentFrame != null) {
            strategy.onAccess(residentFrame, page, timestamp);
            return true; // hit
        }

        Frame targetFrame = findFreeFrame();
        Page evictedPage = null;

        if (targetFrame == null) {
            targetFrame = strategy.selectVictim(frames, timestamp);
            evictedPage = targetFrame.getResidentPage();
        }

        targetFrame.load(page, timestamp);
        strategy.onAccess(targetFrame, page, timestamp);

        notifyFault(new PageFaultEvent(page, evictedPage, timestamp));
        return false; // fault
    }

    /** Runs an entire reference sequence through this manager, returning fault count. */
    public int runSequence(List<MemoryReference> references) {
        int faults = 0;
        for (MemoryReference reference : references) {
            boolean hit = accessPage(reference);
            if (!hit) faults++;
        }
        return faults;
    }

    private Frame findFrameHolding(Page page) {
        for (Frame frame : frames) {
            if (!frame.isFree() && frame.getResidentPage().equals(page)) {
                return frame;
            }
        }
        return null;
    }

    private Frame findFreeFrame() {
        for (Frame frame : frames) {
            if (frame.isFree()) return frame;
        }
        return null;
    }

    private void notifyFault(PageFaultEvent event) {
        for (PageFaultObserver observer : observers) {
            observer.onPageFault(event);
        }
    }

    public List<Frame> getFrames() {
        return List.copyOf(frames);
    }

    public String getStrategyName() {
        return strategy.getName();
    }
}
