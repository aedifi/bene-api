package aedifi.bene.api.command;

public record TimeOfDay(
        int ticks,
        String label) {
    public TimeOfDay {
        if (ticks < 0 || ticks > 23_999) {
            throw new IllegalArgumentException("Minecraft ticks must be in range 0..23999.");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("Time label cannot be blank.");
        }
    }
}
