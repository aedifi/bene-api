package aedifi.bene.api.command;

public record CommandMessages(
        String noPermissionMessage,
        String invalidArgumentMessage,
        String usageMessage,
        String internalErrorMessage) {
    public static CommandMessages defaults() {
        return new CommandMessages(null, null, null, null);
    }
}
