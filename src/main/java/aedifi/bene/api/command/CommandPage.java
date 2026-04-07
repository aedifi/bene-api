package aedifi.bene.api.command;

import java.util.List;

public record CommandPage<T>(
        List<T> items,
        int page,
        int pageSize,
        int totalItems,
        int totalPages) {
    public CommandPage {
        items = List.copyOf(items == null ? List.of() : items);
        if (page < 1) {
            throw new IllegalArgumentException("Page must be >= 1.");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be >= 1.");
        }
        if (totalItems < 0) {
            throw new IllegalArgumentException("Total items cannot be negative.");
        }
        if (totalPages < 1) {
            throw new IllegalArgumentException("Total pages must be >= 1.");
        }
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
