package aedifi.bene.api.command;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CommandSpec {
    private final String root;
    private final List<String> aliases;
    private final String description;
    private final String usage;
    private final String permission;
    private final List<CommandRoute> routes;
    private final CommandMessages messages;

    private CommandSpec(
            final String root,
            final List<String> aliases,
            final String description,
            final String usage,
            final String permission,
            final List<CommandRoute> routes,
            final CommandMessages messages) {
        this.root = root;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.routes = routes;
        this.messages = messages;
    }

    public static Builder builder(final String root) {
        return new Builder(root);
    }

    public String root() {
        return root;
    }

    public List<String> aliases() {
        return aliases;
    }

    public String description() {
        return description;
    }

    public String usage() {
        return usage;
    }

    public String permission() {
        return permission;
    }

    public List<CommandRoute> routes() {
        return routes;
    }

    public CommandMessages messages() {
        return messages;
    }

    public String effectiveUsage() {
        if (usage != null && !usage.isBlank()) {
            return usage;
        }
        final Set<String> firstSegments = new LinkedHashSet<>();
        for (final CommandRoute route : routes) {
            if (route.segments().isEmpty()) {
                continue;
            }
            final CommandSegment first = route.segments().getFirst();
            if (first instanceof LiteralSegment literal) {
                firstSegments.add(literal.token().toLowerCase(Locale.ROOT));
            } else {
                firstSegments.add(first.usageFragment());
            }
        }
        if (firstSegments.isEmpty()) {
            return "&r/" + root;
        }
        final String joined = String.join(" | ", firstSegments);
        return "&r/" + root + " <" + joined + ">";
    }

    public static final class Builder {
        private final String root;
        private final List<String> aliases = new ArrayList<>();
        private final List<CommandRoute> routes = new ArrayList<>();
        private String description;
        private String usage;
        private String permission;
        private CommandMessages messages = CommandMessages.defaults();

        private Builder(final String root) {
            if (root == null || root.isBlank()) {
                throw new IllegalArgumentException("Root command cannot be blank.");
            }
            this.root = root.trim();
        }

        public Builder alias(final String alias) {
            if (alias != null && !alias.isBlank()) {
                aliases.add(alias.trim());
            }
            return this;
        }

        public Builder aliases(final List<String> aliases) {
            if (aliases != null) {
                for (final String alias : aliases) {
                    alias(alias);
                }
            }
            return this;
        }

        public Builder aliases(final String... aliases) {
            if (aliases != null) {
                for (final String alias : aliases) {
                    alias(alias);
                }
            }
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder usage(final String usage) {
            this.usage = usage;
            return this;
        }

        public Builder permission(final String permission) {
            this.permission = permission;
            return this;
        }

        public Builder messages(final CommandMessages messages) {
            if (messages != null) {
                this.messages = messages;
            }
            return this;
        }

        public Builder route(final CommandRoute route) {
            if (route != null) {
                this.routes.add(route);
            }
            return this;
        }

        public CommandSpec build() {
            return new CommandSpec(
                    root,
                    List.copyOf(new LinkedHashSet<>(aliases)),
                    description,
                    usage,
                    permission,
                    List.copyOf(routes),
                    messages == null ? CommandMessages.defaults() : messages);
        }
    }
}
