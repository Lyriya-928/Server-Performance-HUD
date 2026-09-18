package dev.saku.serverperformancehud.local;

import dev.saku.serverperformancehud.metrics.DataSource;
import dev.saku.serverperformancehud.metrics.MetricsProvider;
import dev.saku.serverperformancehud.metrics.MetricsStore;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;
import dev.saku.serverperformancehud.metrics.ServerMetrics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/** Reads the running Integrated Server directly; it never sends a chat command. */
public final class LocalMetricsProvider implements MetricsProvider {
    private final Minecraft minecraft;
    private final MetricsStore store;
    private boolean running;

    public LocalMetricsProvider(Minecraft minecraft, MetricsStore store) {
        this.minecraft = minecraft;
        this.store = store;
    }

    public void tick() {
        if (!running || !minecraft.hasSingleplayerServer() || minecraft.player == null) return;
        IntegratedServer server = minecraft.getSingleplayerServer();
        if (server == null) return;
        long nanos = server.getAverageTickTimeNanos();
        if (nanos <= 0) return;
        double mspt = nanos / 1_000_000.0;
        double tps = Math.min(20.0, 1000.0 / mspt);
        double utilization = Math.min(100.0, mspt / 50.0 * 100.0);
        ServerLevel level = server.getLevel(minecraft.level.dimension());
        Integer chunks = level == null ? null : level.getChunkSource().getLoadedChunksCount();
        Integer entities = level == null ? null : (int) StreamSupport.stream(level.getAllEntities().spliterator(), false).count();
        store.publish(new ServerMetrics(DataSource.LOCAL,
                minecraft.level.dimension().identifier().toString(),
                minecraft.player.blockPosition().getX(), minecraft.player.blockPosition().getY(), minecraft.player.blockPosition().getZ(),
                Map.of("rolling", new PeriodMetrics(utilization, mspt, tps)), chunks,
                server.getPlayerList().getPlayerCount(), entities, System.currentTimeMillis()));
    }

    @Override public void start() { running = true; }
    @Override public void stop() { running = false; }
    @Override public Optional<ServerMetrics> getLatestMetrics() { return store.latest(); }
}
