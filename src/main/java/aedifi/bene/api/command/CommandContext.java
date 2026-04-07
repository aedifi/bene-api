package aedifi.bene.api.command;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.command.CommandSender;

public final class CommandContext {
    private final CommandSender sender;
    private final String label;
    private final List<String> rawArgs;
    private final Map<String, Object> arguments;

    public CommandContext(
            final CommandSender sender,
            final String label,
            final List<String> rawArgs,
            final Map<String, Object> arguments) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.label = Objects.requireNonNull(label, "label");
        this.rawArgs = List.copyOf(rawArgs == null ? List.of() : rawArgs);
        this.arguments = Map.copyOf(arguments == null ? Map.of() : arguments);
    }

    public CommandSender sender() {
        return sender;
    }

    public String label() {
        return label;
    }

    public List<String> rawArgs() {
        return rawArgs;
    }

    public Map<String, Object> arguments() {
        return arguments;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> argument(final String name, final Class<T> type) {
        final Object value = arguments.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Argument '" + name + "' is " + value.getClass().getName()
                            + ", not " + type.getName() + ".");
        }
        return Optional.of((T) value);
    }

    public <T> T require(final String name, final Class<T> type) {
        return argument(name, type)
                .orElseThrow(() -> new IllegalArgumentException("Missing argument: " + name));
    }
}
