package aedifi.bene.api.command;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class LiteralSegment implements CommandSegment {
    private final String token;
    private final List<String> aliases;
    private final List<String> variants;

    public LiteralSegment(final String token, final List<String> aliases) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Literal token cannot be blank.");
        }
        this.token = token;
        this.aliases = aliases == null ? List.of() : List.copyOf(aliases);
        this.variants = buildVariants(token, this.aliases);
    }

    public String token() {
        return token;
    }

    public List<String> aliases() {
        return aliases;
    }

    public List<String> variants() {
        return variants;
    }

    public boolean matches(final String input) {
        if (input == null) {
            return false;
        }
        final String normalized = normalize(input);
        for (final String variant : variants) {
            if (normalize(variant).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String usageFragment() {
        return token;
    }

    private static List<String> buildVariants(final String token, final List<String> aliases) {
        final java.util.LinkedHashSet<String> values = new java.util.LinkedHashSet<>();
        values.add(token);
        for (final String alias : aliases) {
            if (alias != null && !alias.isBlank()) {
                values.add(alias);
            }
        }
        return List.copyOf(values);
    }

    private static String normalize(final String value) {
        return Objects.requireNonNull(value, "value").toLowerCase(Locale.ROOT).trim();
    }
}
