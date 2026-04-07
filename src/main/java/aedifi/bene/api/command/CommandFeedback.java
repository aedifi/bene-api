package aedifi.bene.api.command;

import aedifi.bene.api.util.MessageUtil;
import net.kyori.adventure.text.Component;

public final class CommandFeedback {
    public static final String DEFAULT_INTERNAL_ERROR =
            "An internal error occurred while running this command.";

    private CommandFeedback() {}

    public static Component render(final CommandSpec spec, final CommandDispatchResult result) {
        final CommandMessages messages = spec.messages();
        return switch (result.status()) {
            case EXECUTED -> Component.empty();
            case NO_PERMISSION -> MessageUtil.errorOrDefault(
                    firstNonBlank(result.message(), messages.noPermissionMessage()),
                    MessageUtil.DEFAULT_NO_PERMISSION);
            case INVALID_ARGUMENT -> MessageUtil.invalidSubcommand(
                    result.token() == null ? "?" : result.token(),
                    firstNonBlank(result.message(), messages.invalidArgumentMessage()));
            case USAGE -> MessageUtil.component(firstNonBlank(
                    result.message(),
                    messages.usageMessage(),
                    spec.effectiveUsage()));
            case USER_ERROR -> MessageUtil.errorOrDefault(result.message(), "Command execution failed.");
            case INTERNAL_ERROR -> MessageUtil.errorOrDefault(
                    firstNonBlank(result.message(), messages.internalErrorMessage()),
                    DEFAULT_INTERNAL_ERROR);
        };
    }

    private static String firstNonBlank(final String... values) {
        for (final String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
