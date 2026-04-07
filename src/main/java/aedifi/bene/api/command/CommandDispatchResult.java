package aedifi.bene.api.command;

public final class CommandDispatchResult {
    public enum Status {
        EXECUTED,
        NO_PERMISSION,
        INVALID_ARGUMENT,
        USAGE,
        USER_ERROR,
        INTERNAL_ERROR
    }

    private static final CommandDispatchResult EXECUTED = new CommandDispatchResult(Status.EXECUTED, null, null, null);

    private final Status status;
    private final String message;
    private final String token;
    private final Throwable cause;

    private CommandDispatchResult(
            final Status status,
            final String message,
            final String token,
            final Throwable cause) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.cause = cause;
    }

    public static CommandDispatchResult executed() {
        return EXECUTED;
    }

    public static CommandDispatchResult noPermission(final String message) {
        return new CommandDispatchResult(Status.NO_PERMISSION, message, null, null);
    }

    public static CommandDispatchResult invalidArgument(final String token, final String message) {
        return new CommandDispatchResult(Status.INVALID_ARGUMENT, message, token, null);
    }

    public static CommandDispatchResult usage(final String message) {
        return new CommandDispatchResult(Status.USAGE, message, null, null);
    }

    public static CommandDispatchResult userError(final String message) {
        return new CommandDispatchResult(Status.USER_ERROR, message, null, null);
    }

    public static CommandDispatchResult internalError(final String message, final Throwable cause) {
        return new CommandDispatchResult(Status.INTERNAL_ERROR, message, null, cause);
    }

    public Status status() {
        return status;
    }

    public String message() {
        return message;
    }

    public String token() {
        return token;
    }

    public Throwable cause() {
        return cause;
    }
}
