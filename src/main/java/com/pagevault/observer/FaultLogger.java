package com.pagevault.observer;

import com.pagevault.core.PageFaultEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Keeps a full ordered history of every fault event, for later inspection or replay. */
public class FaultLogger implements PageFaultObserver {

    private final List<PageFaultEvent> history = new ArrayList<>();

    @Override
    public void onPageFault(PageFaultEvent event) {
        history.add(event);
    }

    public List<PageFaultEvent> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void printHistory() {
        for (PageFaultEvent event : history) {
            System.out.println("  " + event);
        }
    }
}
