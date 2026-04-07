package aedifi.bene.api.command;

public final class CommandFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public enum Kind {
        NO_PERMISSION,
        INVALID_ARGUMENT,
        USAGE,
        USER_ERROR,
        INTERNAL_ERROR
    }

    private final Kind kind;
    private final String invalidToken;

    private CommandFailure(final Kind kind, final String message, final String invalidToken) {
        super(message);
        this.kind = kind;
        this.invalidToken = invalidToken;
    }

    public static CommandFailure noPermission(final String message) {
        return new CommandFailure(Kind.NO_PERMISSION, message, null);
    }

    public static CommandFailure invalidArgument(final String token, final String message) {
        return new CommandFailure(Kind.INVALID_ARGUMENT, message, token);
    }

    public static CommandFailure usage(final String message) {
        return new CommandFailure(Kind.USAGE, message, null);
    }

    public static CommandFailure userError(final String message) {
        return new CommandFailure(Kind.USER_ERROR, message, null);
    }

    public static CommandFailure internalError(final String message) {
        return new CommandFailure(Kind.INTERNAL_ERROR, message, null);
    }

    public Kind kind() {
        return kind;
    }

    public String invalidToken() {
        return invalidToken;
    }
}
