package aedifi.bene.api.command;

import java.util.List;

public final class CommandPaginator {
    private CommandPaginator() {}

    public static <T> CommandPage<T> paginate(
            final List<T> values,
            final int pageSize,
            final int requestedPage) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be >= 1.");
        }
        final List<T> items = values == null ? List.of() : List.copyOf(values);
        final int totalItems = items.size();
        final int totalPages = Math.max(1, (int) Math.ceil((double) totalItems / (double) pageSize));
        final int page = clamp(requestedPage, 1, totalPages);

        final int start = (page - 1) * pageSize;
        final int end = Math.min(totalItems, start + pageSize);
        final List<T> slice = start >= end ? List.of() : items.subList(start, end);
        return new CommandPage<>(slice, page, pageSize, totalItems, totalPages);
    }

    public static int parsePageOrDefault(final String rawToken, final int fallbackPage) {
        if (rawToken == null || rawToken.isBlank()) {
            return fallbackPage;
        }
        try {
            final int page = Integer.parseInt(rawToken.trim());
            return Math.max(page, 1);
        } catch (final NumberFormatException ignored) {
            return fallbackPage;
        }
    }

    private static int clamp(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
