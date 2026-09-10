package com.pagevault.core;

import java.util.Objects;

/** A logical page number a process references — the unit of virtual memory. */
public final class Page {
    private final int pageNumber;

    public Page(int pageNumber) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() { return pageNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        return pageNumber == ((Page) o).pageNumber;
    }

    @Override
    public int hashCode() { return Objects.hash(pageNumber); }

    @Override
    public String toString() { return "P" + pageNumber; }
}
