package dev.saku.serverperformancehud.prediction;

import dev.saku.serverperformancehud.metrics.DataSource;
import dev.saku.serverperformancehud.metrics.MetricsProvider;
import dev.saku.serverperformancehud.metrics.MetricsStore;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;
import dev.saku.serverperformancehud.metrics.ServerMetrics;

import java.util.Map;
import java.util.Optional;

/** Estimates TPS from server time packet arrival intervals; it intentionally never creates MSPT. */
public final class PredictionMetricsProvider implements MetricsProvider {
    private final MetricsStore store;
    private long previousPacketNanos;

    public PredictionMetricsProvider(MetricsStore store) { this.store = store; }
    public void onServerTimePacket(long nowNanos, String world, int x, int y, int z) {
        if (previousPacketNanos != 0) {
            double seconds = (nowNanos - previousPacketNanos) / 1_000_000_000.0;
            if (seconds > 0.05 && seconds < 10.0) {
                double tps = Math.min(20.0, Math.max(0.0, 20.0 / seconds));
                store.publish(new ServerMetrics(DataSource.PREDICTION, world, x, y, z,
                        Map.of("rolling", new PeriodMetrics(null, null, tps)), null, null, null,
                        System.currentTimeMillis()));
            }
        }
        previousPacketNanos = nowNanos;
    }
    @Override public void start() { previousPacketNanos = 0; }
    @Override public void stop() { previousPacketNanos = 0; }
    @Override public Optional<ServerMetrics> getLatestMetrics() { return store.latest(); }
}

