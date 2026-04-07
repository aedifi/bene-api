package aedifi.bene.api.command;

public sealed interface CommandSegment permits LiteralSegment, ArgumentSegment {
    String usageFragment();
}
