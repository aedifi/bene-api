package aedifi.bene.api.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandRoute {
    private final List<CommandSegment> segments;
    private final String permission;
    private final String description;
    private final CommandHandler handler;

    private CommandRoute(
            final List<CommandSegment> segments,
            final String permission,
            final String description,
            final CommandHandler handler) {
        this.segments = List.copyOf(segments);
        this.permission = permission;
        this.description = description;
        this.handler = handler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<CommandSegment> segments() {
        return segments;
    }

    public String permission() {
        return permission;
    }

    public String description() {
        return description;
    }

    public CommandHandler handler() {
        return handler;
    }

    public int literalCount() {
        int count = 0;
        for (final CommandSegment segment : segments) {
            if (segment instanceof LiteralSegment) {
                count++;
            }
        }
        return count;
    }

    public String usageSuffix() {
        if (segments.isEmpty()) {
            return "";
        }
        final StringBuilder usage = new StringBuilder();
        for (final CommandSegment segment : segments) {
            if (!usage.isEmpty()) {
                usage.append(' ');
            }
            usage.append(segment.usageFragment());
        }
        return usage.toString();
    }

    public static final class Builder {
        private final List<CommandSegment> segments = new ArrayList<>();
        private String permission;
        private String description;
        private CommandHandler handler;

        private Builder() {}

        public Builder literal(final String token) {
            segments.add(new LiteralSegment(token, List.of()));
            return this;
        }

        public Builder literal(final String token, final List<String> aliases) {
            segments.add(new LiteralSegment(token, aliases));
            return this;
        }

        public Builder literal(final String token, final String... aliases) {
            segments.add(new LiteralSegment(token, aliases == null ? List.of() : List.of(aliases)));
            return this;
        }

        public <T> Builder argument(final String name, final CommandParser<T> parser) {
            segments.add(new ArgumentSegment<>(name, parser));
            return this;
        }

        public Builder permission(final String permission) {
            this.permission = permission;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder executes(final CommandHandler handler) {
            this.handler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public CommandRoute build() {
            if (handler == null) {
                throw new IllegalStateException("Route handler cannot be null.");
            }
            return new CommandRoute(segments, permission, description, handler);
        }
    }
}
