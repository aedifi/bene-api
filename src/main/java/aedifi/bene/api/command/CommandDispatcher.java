package aedifi.bene.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class CommandDispatcher {
    private final CommandSpec spec;
    private final List<CommandRoute> orderedRoutes;

    public CommandDispatcher(final CommandSpec spec) {
        this.spec = Objects.requireNonNull(spec, "spec");
        final Comparator<CommandRoute> specificity = Comparator
                .comparingInt(CommandRoute::literalCount)
                .thenComparingInt(route -> route.segments().size());
        this.orderedRoutes = spec.routes().stream().sorted(specificity.reversed()).toList();
    }

    public CommandDispatchResult dispatch(
            final CommandSender sender,
            final String label,
            final String[] rawArgs) {
        Objects.requireNonNull(sender, "sender");
        final String[] args = sanitizeArgs(rawArgs);

        if (!hasPermission(sender, spec.permission())) {
            return CommandDispatchResult.noPermission(null);
        }

        RouteProbe bestProbe = null;
        boolean deniedExactMatch = false;
        for (final CommandRoute route : orderedRoutes) {
            final RouteProbe probe = probe(route, sender, args);
            if (bestProbe == null || probe.matchedSegments() > bestProbe.matchedSegments()) {
                bestProbe = probe;
            }
            if (!probe.exactMatch()) {
                continue;
            }

            if (!hasPermission(sender, route.permission())) {
                deniedExactMatch = true;
                continue;
            }

            final CommandContext context = new CommandContext(
                    sender,
                    label,
                    Arrays.asList(args),
                    probe.arguments());
            try {
                route.handler().execute(context);
                return CommandDispatchResult.executed();
            } catch (final CommandFailure failure) {
                return fromFailure(failure);
            } catch (final Exception ex) {
                return CommandDispatchResult.internalError(null, ex);
            }
        }

        if (deniedExactMatch) {
            return CommandDispatchResult.noPermission(null);
        }

        if (args.length == 0 || bestProbe == null || bestProbe.matchedSegments() >= args.length) {
            return CommandDispatchResult.usage(spec.effectiveUsage());
        }

        final int invalidIndex = Math.min(bestProbe.matchedSegments(), args.length - 1);
        return CommandDispatchResult.invalidArgument(args[invalidIndex], null);
    }

    public List<String> complete(final CommandSender sender, final String[] rawArgs) {
        Objects.requireNonNull(sender, "sender");
        if (!hasPermission(sender, spec.permission())) {
            return List.of();
        }

        final String[] args = sanitizeArgs(rawArgs);
        final String prefix = args.length == 0 ? "" : args[args.length - 1];
        final String[] prior = args.length == 0 ? new String[0] : Arrays.copyOf(args, args.length - 1);

        final LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        for (final CommandRoute route : orderedRoutes) {
            if (!hasPermission(sender, route.permission())) {
                continue;
            }
            if (!matchesPrefix(route, sender, prior)) {
                continue;
            }
            if (prior.length >= route.segments().size()) {
                continue;
            }
            final CommandSegment nextSegment = route.segments().get(prior.length);
            if (nextSegment instanceof LiteralSegment literal) {
                for (final String variant : literal.variants()) {
                    if (startsWithIgnoreCase(variant, prefix)) {
                        suggestions.add(variant);
                    }
                }
            } else if (nextSegment instanceof ArgumentSegment<?> argument) {
                for (final String suggestion : argument.suggest(sender, prefix)) {
                    if (suggestion != null && !suggestion.isBlank() && startsWithIgnoreCase(suggestion, prefix)) {
                        suggestions.add(suggestion);
                    }
                }
            }
        }
        return suggestions.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private RouteProbe probe(final CommandRoute route, final CommandSender sender, final String[] args) {
        final List<CommandSegment> segments = route.segments();
        final int toMatch = Math.min(args.length, segments.size());
        final Map<String, Object> parsed = new LinkedHashMap<>();

        for (int index = 0; index < toMatch; index++) {
            final CommandSegment segment = segments.get(index);
            final String token = args[index];
            if (segment instanceof LiteralSegment literal) {
                if (!literal.matches(token)) {
                    return new RouteProbe(false, index, parsed);
                }
            } else if (segment instanceof ArgumentSegment<?> argument) {
                try {
                    final Object value = argument.parse(sender, token);
                    parsed.put(argument.name(), value);
                } catch (final CommandParseException ignored) {
                    return new RouteProbe(false, index, parsed);
                }
            }
        }

        final boolean exact = args.length == segments.size();
        final int matchedSegments = exact ? segments.size() : toMatch;
        return new RouteProbe(exact, matchedSegments, parsed);
    }

    private boolean matchesPrefix(
            final CommandRoute route,
            final CommandSender sender,
            final String[] priorTokens) {
        final List<CommandSegment> segments = route.segments();
        if (priorTokens.length > segments.size()) {
            return false;
        }
        for (int index = 0; index < priorTokens.length; index++) {
            final CommandSegment segment = segments.get(index);
            final String token = priorTokens[index];
            if (segment instanceof LiteralSegment literal) {
                if (!literal.matches(token)) {
                    return false;
                }
                continue;
            }
            if (segment instanceof ArgumentSegment<?> argument) {
                try {
                    argument.parse(sender, token);
                } catch (final CommandParseException ignored) {
                    return false;
                }
            }
        }
        return true;
    }

    private static CommandDispatchResult fromFailure(final CommandFailure failure) {
        return switch (failure.kind()) {
            case NO_PERMISSION -> CommandDispatchResult.noPermission(failure.getMessage());
            case INVALID_ARGUMENT -> CommandDispatchResult.invalidArgument(
                    failure.invalidToken(),
                    failure.getMessage());
            case USAGE -> CommandDispatchResult.usage(failure.getMessage());
            case USER_ERROR -> CommandDispatchResult.userError(failure.getMessage());
            case INTERNAL_ERROR -> CommandDispatchResult.internalError(failure.getMessage(), failure);
        };
    }

    private static boolean hasPermission(final CommandSender sender, final String permission) {
        return permission == null || permission.isBlank() || sender.hasPermission(permission);
    }

    private static String[] sanitizeArgs(final String[] rawArgs) {
        if (rawArgs == null) {
            return new String[0];
        }
        return Arrays.copyOf(rawArgs, rawArgs.length);
    }

    private static boolean startsWithIgnoreCase(final String candidate, final String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return candidate.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }

    private record RouteProbe(
            boolean exactMatch,
            int matchedSegments,
            Map<String, Object> arguments) {}
}
