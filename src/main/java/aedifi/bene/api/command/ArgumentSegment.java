package aedifi.bene.api.command;

import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class ArgumentSegment<T> implements CommandSegment {
    private final String name;
    private final CommandParser<T> parser;

    public ArgumentSegment(final String name, final CommandParser<T> parser) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Argument name cannot be blank.");
        }
        this.name = name;
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    public String name() {
        return name;
    }

    public CommandParser<T> parser() {
        return parser;
    }

    public T parse(final CommandSender sender, final String token) throws CommandParseException {
        return parser.parse(sender, token);
    }

    public List<String> suggest(final CommandSender sender, final String tokenPrefix) {
        return parser.suggest(sender, tokenPrefix);
    }

    @Override
    public String usageFragment() {
        return "<" + name + ">";
    }
}
