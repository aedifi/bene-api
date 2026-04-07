package aedifi.bene.api.command;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface CommandParser<T> {
    T parse(CommandSender sender, String token) throws CommandParseException;

    default List<String> suggest(final CommandSender sender, final String inputPrefix) {
        return List.of();
    }

    default String usageToken() {
        return "<value>";
    }
}
