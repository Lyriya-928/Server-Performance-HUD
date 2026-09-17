package dev.saku.serverperformancehud.command;

import dev.saku.serverperformancehud.metrics.MetricsProvider;
import dev.saku.serverperformancehud.metrics.MetricsStatus;
import dev.saku.serverperformancehud.metrics.MetricsStore;
import dev.saku.serverperformancehud.metrics.ServerMetrics;
import dev.saku.serverperformancehud.parser.CommandSession;
import dev.saku.serverperformancehud.parser.FoliaTpsRegionParser;
import dev.saku.serverperformancehud.parser.Message;
import dev.saku.serverperformancehud.parser.ParseError;
import dev.saku.serverperformancehud.parser.ParseResult;
import net.minecraft.client.Minecraft;

import java.util.Optional;
import java.util.function.Consumer;

public final class CommandMetricsProvider implements MetricsProvider {
    public static final int DEFAULT_INTERVAL_SECONDS = 5;
    public static final int DEFAULT_TIMEOUT_SECONDS = 3;
    private final Minecraft minecraft;
    private final MetricsStore store;
    private final FoliaTpsRegionParser parser = new FoliaTpsRegionParser();
    private String command = "/tps region";
    private int intervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
    private CommandSession session;
    private long nextRequestTick;
    private long deadlineTick;
    private boolean running;
    private long currentTick;
    private boolean probeOnly;
    private Consumer<Boolean> probeResult;

    public CommandMetricsProvider(Minecraft minecraft, MetricsStore store) {
        this.minecraft = minecraft;
        this.store = store;
    }

    public void configure(String command, int intervalSeconds, int timeoutSeconds) {
        this.command = command == null || command.isBlank() ? "/tps region" : command;
        this.intervalSeconds = Math.max(1, intervalSeconds);
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
    }

    public void tick(long clientTick) {
        currentTick = clientTick;
        if (!running || minecraft.player == null) return;
        if (session != null && clientTick >= deadlineTick) {
            parser.timeout();
            session = null;
            store.status(MetricsStatus.TIMEOUT);
            nextRequestTick = clientTick + intervalTicks();
            finishProbe(false);
        }
        if (running && !probeOnly && session == null && clientTick >= nextRequestTick) request(clientTick, true);
    }

    public void testCommand() {
        if (minecraft.player != null && session == null) request(currentTick, false);
    }

    /** Sends one isolated capability probe and never schedules normal polling. */
    public void probe(Consumer<Boolean> result) {
        if (minecraft.player == null || session != null || probeOnly) return;
        running = true;
        probeOnly = true;
        probeResult = result;
        request(currentTick, false);
    }

    private void request(long clientTick, boolean scheduleNext) {
        parser.reset();
        session = new CommandSession(System.currentTimeMillis());
        deadlineTick = clientTick + timeoutSeconds * 20L;
        if (scheduleNext) nextRequestTick = clientTick + intervalSeconds * 20L;
        store.status(MetricsStatus.UPDATING);
        String value = command.startsWith("/") ? command.substring(1) : command;
        minecraft.player.connection.sendCommand(value);
    }

    public void accept(String raw, long receivedAt) {
        if (session == null) return;
        if (!session.accepts(receivedAt)) return;
        ParseResult result = parser.accept(Message.of(raw, receivedAt));
        if (result instanceof ParseResult.Success success) {
            store.publish(success.metrics());
            session = null;
            nextRequestTick = currentTick + intervalTicks();
            finishProbe(true);
        } else if (result instanceof ParseResult.Failure failure) {
            session = null;
            store.status(statusFor(failure.error()));
            nextRequestTick = currentTick + intervalTicks();
            finishProbe(false);
        }
    }

    /**
     * Accepts a likely automated response and reports whether it should be hidden.
     * Manual commands remain visible because no active session owns their messages.
     */
    public boolean acceptAutomatedMessage(String raw, long receivedAt) {
        if (session == null || !FoliaTpsRegionParser.looksLikeResponse(raw)) return false;
        if (!session.accepts(receivedAt)) return false;
        ParseResult before = parser.accept(Message.of(raw, receivedAt));
        if (before instanceof ParseResult.Success success) {
            store.publish(success.metrics());
            session = null;
            nextRequestTick = currentTick + intervalTicks();
            finishProbe(true);
        } else if (before instanceof ParseResult.Failure failure) {
            session = null;
            store.status(statusFor(failure.error()));
            nextRequestTick = currentTick + intervalTicks();
            finishProbe(false);
        }
        return true;
    }

    private void finishProbe(boolean success) {
        if (!probeOnly) return;
        probeOnly = false;
        Consumer<Boolean> callback = probeResult;
        probeResult = null;
        if (callback != null) callback.accept(success);
    }

    private static MetricsStatus statusFor(ParseError error) {
        return switch (error) {
            case NO_PERMISSION -> MetricsStatus.NO_PERMISSION;
            case COMMAND_NOT_FOUND -> MetricsStatus.COMMAND_NOT_FOUND;
            case TIMEOUT -> MetricsStatus.TIMEOUT;
            default -> MetricsStatus.PARSE_ERROR;
        };
    }

    private long intervalTicks() { return intervalSeconds * 20L; }

    @Override public void start() { running = true; nextRequestTick = 0; }
    @Override public void stop() { running = false; session = null; probeOnly = false; probeResult = null; parser.reset(); }
    @Override public Optional<ServerMetrics> getLatestMetrics() { return store.latest(); }
    public MetricsStatus status() { return store.status(); }
}
