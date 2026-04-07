package aedifi.bene.api.command;

@FunctionalInterface
public interface CommandHandler {
    void execute(CommandContext context) throws Exception;
}
