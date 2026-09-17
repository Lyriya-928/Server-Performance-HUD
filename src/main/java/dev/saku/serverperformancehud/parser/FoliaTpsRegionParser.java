package dev.saku.serverperformancehud.parser;

import dev.saku.serverperformancehud.metrics.DataSource;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;
import dev.saku.serverperformancehud.metrics.ServerMetrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Order-independent parser for the human-readable Folia /tps region response. */
public final class FoliaTpsRegionParser implements CommandParser {
    private static final Pattern REGION = Pattern.compile(
            "(?i)\\[\\s*w\\s*:\\s*['\"]?([^,'\"]+)['\"]?\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)");
    private static final Pattern PERIOD = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?[smh])\\s*:");
    private static final Pattern UTIL = Pattern.compile("(?i)([-+]?\\d+(?:[.,]\\d+)?)\\s*%\\s*util");
    private static final Pattern MSPT = Pattern.compile("(?i)([-+]?\\d+(?:[.,]\\d+)?)\\s*MSPT");
    private static final Pattern TPS = Pattern.compile("(?i)([-+]?\\d+(?:[.,]\\d+)?)\\s*TPS");
    private static final Pattern CHUNKS = Pattern.compile("(?i)Chunks\\s*:\\s*([\\d,]+)");
    private static final Pattern PLAYERS = Pattern.compile("(?i)Players\\s*:\\s*([\\d,]+)");
    private static final Pattern ENTITIES = Pattern.compile("(?i)Entities\\s*:\\s*([\\d,]+)");
    private static final Pattern RESPONSE_MARKER = Pattern.compile(
            "(?i)(?:Region\\s+around\\s+block\\s*\\[|\\b\\d+(?:\\.\\d+)?[smh]\\s*:\\s*.*\\b(?:util|MSPT|TPS)\\b|\\b(?:Chunks|Players|Entities)\\s*:|\\b(?:unknown\\s+command|command\\s+not\\s+found|no\\s+permission|do\\s+not\\s+have\\s+permission|not\\s+allowed)\\b)");

    private ParserState state = ParserState.IDLE;
    private String world;
    private int x, y, z;
    private final Map<String, PeriodMetrics> periods = new LinkedHashMap<>();
    private Integer chunks, players, entities;

    @Override
    public ParseResult accept(Message message) {
        if (state == ParserState.COMPLETE) return new ParseResult.InProgress();
        if (state == ParserState.IDLE) state = ParserState.COLLECTING;
        String line = message.normalized();
        String lower = line.toLowerCase();
        if (lower.contains("no permission") || lower.contains("do not have permission") || lower.contains("not allowed")) {
            return fail(ParseError.NO_PERMISSION);
        }
        if (lower.contains("unknown command") || lower.contains("command not found")) {
            return fail(ParseError.COMMAND_NOT_FOUND);
        }

        try {
            Matcher regionMatcher = REGION.matcher(line);
            if (regionMatcher.find()) {
                world = regionMatcher.group(1).trim();
                x = Integer.parseInt(regionMatcher.group(2));
                y = Integer.parseInt(regionMatcher.group(3));
                z = Integer.parseInt(regionMatcher.group(4));
            }

            Matcher periodMatcher = PERIOD.matcher(line);
            if (periodMatcher.find()) {
                String name = periodMatcher.group(1).toLowerCase();
                Double utilization = number(UTIL, line);
                Double mspt = number(MSPT, line);
                Double tps = number(TPS, line);
                if ((utilization != null && !valid(utilization, 0, 100))
                        || (mspt != null && !valid(mspt, 0, Double.MAX_VALUE))
                        || (tps != null && !valid(tps, 0, 20))) {
                    return fail(ParseError.INVALID_DATA);
                }
                if (utilization != null || mspt != null || tps != null) {
                    periods.put(name, new PeriodMetrics(utilization, mspt, tps));
                }
            }

            chunks = updateCount(CHUNKS, line, chunks);
            players = updateCount(PLAYERS, line, players);
            entities = updateCount(ENTITIES, line, entities);
        } catch (NumberFormatException error) {
            return fail(ParseError.INVALID_DATA);
        }
        if (world != null && !periods.isEmpty() && chunks != null && players != null && entities != null) {
            state = ParserState.COMPLETE;
            return new ParseResult.Success(new ServerMetrics(DataSource.COMMAND, world, x, y, z,
                    periods, chunks, players, entities, message.receivedAt()));
        }
        return new ParseResult.InProgress();
    }

    public ParseResult timeout() { return fail(ParseError.TIMEOUT); }
    public ParserState state() { return state; }

    /** Conservative classifier used before cancelling a system chat packet. */
    public static boolean looksLikeResponse(String raw) {
        return raw != null && RESPONSE_MARKER.matcher(TextNormalizer.normalize(raw)).find();
    }

    @Override
    public void reset() {
        state = ParserState.RESET;
        world = null;
        periods.clear();
        chunks = players = entities = null;
        state = ParserState.IDLE;
    }

    private ParseResult.Failure fail(ParseError error) {
        reset();
        return new ParseResult.Failure(error);
    }

    private static Double number(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? Double.parseDouble(matcher.group(1).replace(',', '.')) : null;
    }

    private static boolean valid(double value, double min, double max) {
        return Double.isFinite(value) && value >= min && value <= max;
    }

    private static int count(String value) {
        return Integer.parseInt(value.replace(",", ""));
    }

    private static Integer updateCount(Pattern pattern, String line, Integer previous) {
        Matcher matcher = pattern.matcher(line);
        return matcher.find() ? count(matcher.group(1)) : previous;
    }
}
