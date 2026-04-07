package aedifi.bene.api.command;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandParsers {
    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)(ms|s|m|h|d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLOCK_12H = Pattern.compile("^(\\d{1,2})(?::(\\d{2}))?(am|pm)$", Pattern.CASE_INSENSITIVE);
    private static final Map<String, Integer> TIME_ALIASES = buildTimeAliases();

    private CommandParsers() {}

    public static CommandParser<String> word() {
        return new CommandParser<>() {
            @Override
            public String parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a value.");
                }
                return token;
            }

            @Override
            public String usageToken() {
                return "<word>";
            }
        };
    }

    public static CommandParser<Player> player() {
        return new CommandParser<>() {
            @Override
            public Player parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a player.");
                }
                Player player = Bukkit.getPlayerExact(token);
                if (player == null) {
                    player = Bukkit.getPlayer(token);
                }
                if (player == null) {
                    throw new CommandParseException("Unknown online player: " + token);
                }
                return player;
            }

            @Override
            public List<String> suggest(final CommandSender sender, final String inputPrefix) {
                final String prefix = normalize(inputPrefix);
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> normalize(name).startsWith(prefix))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }

            @Override
            public String usageToken() {
                return "<player>";
            }
        };
    }

    public static CommandParser<OfflinePlayer> offlinePlayer() {
        return new CommandParser<>() {
            @Override
            public OfflinePlayer parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a player.");
                }
                try {
                    return Bukkit.getOfflinePlayer(UUID.fromString(token));
                } catch (final IllegalArgumentException ignored) {
                    final Player online = Bukkit.getPlayerExact(token);
                    if (online != null) {
                        return online;
                    }
                    throw new CommandParseException("Unknown player UUID or online player: " + token);
                }
            }

            @Override
            public List<String> suggest(final CommandSender sender, final String inputPrefix) {
                final String prefix = normalize(inputPrefix);
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> normalize(name).startsWith(prefix))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }

            @Override
            public String usageToken() {
                return "<offline-player>";
            }
        };
    }

    public static CommandParser<World> world() {
        return new CommandParser<>() {
            @Override
            public World parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a world.");
                }
                final World world = Bukkit.getWorld(token);
                if (world == null) {
                    throw new CommandParseException("Unknown world: " + token);
                }
                return world;
            }

            @Override
            public List<String> suggest(final CommandSender sender, final String inputPrefix) {
                final String prefix = normalize(inputPrefix);
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> normalize(name).startsWith(prefix))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
            }

            @Override
            public String usageToken() {
                return "<world>";
            }
        };
    }

    public static CommandParser<Duration> duration() {
        return new CommandParser<>() {
            @Override
            public Duration parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a duration.");
                }
                return parseDurationToken(token);
            }

            @Override
            public List<String> suggest(final CommandSender sender, final String inputPrefix) {
                return prefixSuggestions(inputPrefix, List.of("10s", "30s", "5m", "1h", "1d"));
            }

            @Override
            public String usageToken() {
                return "<duration>";
            }
        };
    }

    public static CommandParser<Integer> integer() {
        return integer(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static CommandParser<Integer> integer(final int min, final int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max");
        }
        return new CommandParser<>() {
            @Override
            public Integer parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected an integer value.");
                }
                try {
                    final int value = Integer.parseInt(token);
                    if (value < min || value > max) {
                        throw new CommandParseException(
                                "Expected a number between " + min + " and " + max + ".");
                    }
                    return value;
                } catch (final NumberFormatException ex) {
                    throw new CommandParseException("Expected an integer value.");
                }
            }

            @Override
            public String usageToken() {
                return "<int>";
            }
        };
    }

    public static CommandParser<TimeOfDay> timeOfDay() {
        return new CommandParser<>() {
            @Override
            public TimeOfDay parse(final CommandSender sender, final String token) throws CommandParseException {
                if (token == null || token.isBlank()) {
                    throw new CommandParseException("Expected a time value.");
                }
                return parseTimeToken(token);
            }

            @Override
            public List<String> suggest(final CommandSender sender, final String inputPrefix) {
                return prefixSuggestions(inputPrefix, List.of(
                        "dawn",
                        "noon",
                        "sunset",
                        "midnight",
                        "3am",
                        "6pm",
                        "12000"));
            }

            @Override
            public String usageToken() {
                return "<time>";
            }
        };
    }

    private static Duration parseDurationToken(final String token) throws CommandParseException {
        final String normalized = token.trim();
        try {
            if (normalized.startsWith("P") || normalized.startsWith("p")) {
                return Duration.parse(normalized.toUpperCase(Locale.ROOT));
            }
        } catch (final RuntimeException ex) {
            throw new CommandParseException("Invalid ISO-8601 duration: " + token);
        }

        final Matcher matcher = DURATION_PART.matcher(normalized);
        int cursor = 0;
        long totalMillis = 0L;
        while (matcher.find()) {
            if (matcher.start() != cursor) {
                throw new CommandParseException("Invalid duration format: " + token);
            }
            cursor = matcher.end();
            final long amount = Long.parseLong(matcher.group(1));
            final String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            try {
                totalMillis = Math.addExact(totalMillis, Math.multiplyExact(amount, unitMillis(unit)));
            } catch (final ArithmeticException ex) {
                throw new CommandParseException("Duration is too large: " + token);
            }
        }
        if (cursor != normalized.length() || cursor == 0) {
            throw new CommandParseException("Invalid duration format: " + token);
        }
        return Duration.ofMillis(totalMillis);
    }

    private static TimeOfDay parseTimeToken(final String token) throws CommandParseException {
        final String normalized = normalize(token);
        final Integer aliasTicks = TIME_ALIASES.get(normalized);
        if (aliasTicks != null) {
            return new TimeOfDay(aliasTicks, normalized);
        }

        try {
            final int ticks = Integer.parseInt(normalized);
            if (ticks < 0 || ticks > 23_999) {
                throw new CommandParseException("Ticks must be between 0 and 23999.");
            }
            return new TimeOfDay(ticks, normalized);
        } catch (final NumberFormatException ignored) {
            // Attempt 12-hour parsing.
        }

        final Matcher matcher = CLOCK_12H.matcher(normalized);
        if (!matcher.matches()) {
            throw new CommandParseException("Unsupported time value: " + token);
        }
        final int hour = Integer.parseInt(matcher.group(1));
        final String minuteRaw = matcher.group(2);
        final int minute = minuteRaw == null ? 0 : Integer.parseInt(minuteRaw);
        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
            throw new CommandParseException("Invalid clock time: " + token);
        }

        final boolean pm = "pm".equalsIgnoreCase(matcher.group(3));
        int hour24 = hour % 12;
        if (pm) {
            hour24 += 12;
        }

        final double ticksPerMinute = 1000.0D / 60.0D;
        final int dayOffset = Math.floorMod(hour24 - 6, 24) * 1000;
        final int minuteTicks = (int) Math.round(minute * ticksPerMinute);
        final int ticks = Math.floorMod(dayOffset + minuteTicks, 24_000);
        return new TimeOfDay(ticks, normalized);
    }

    private static long unitMillis(final String unit) {
        return switch (unit) {
            case "ms" -> 1L;
            case "s" -> 1_000L;
            case "m" -> 60_000L;
            case "h" -> 3_600_000L;
            case "d" -> 86_400_000L;
            default -> throw new IllegalArgumentException("Unsupported duration unit: " + unit);
        };
    }

    private static List<String> prefixSuggestions(final String inputPrefix, final List<String> values) {
        final String prefix = normalize(inputPrefix);
        final List<String> results = new ArrayList<>();
        for (final String value : values) {
            if (normalize(value).startsWith(prefix)) {
                results.add(value);
            }
        }
        return results;
    }

    private static String normalize(final String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private static Map<String, Integer> buildTimeAliases() {
        final Map<String, Integer> aliases = new LinkedHashMap<>();
        aliases.put("dawn", 0);
        aliases.put("sunrise", 0);
        aliases.put("morning", 0);
        aliases.put("noon", 6_000);
        aliases.put("midday", 6_000);
        aliases.put("sunset", 12_000);
        aliases.put("dusk", 12_000);
        aliases.put("evening", 12_000);
        aliases.put("midnight", 18_000);
        aliases.put("night", 18_000);
        return Map.copyOf(aliases);
    }
}
