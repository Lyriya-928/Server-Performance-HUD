package dev.saku.serverperformancehud.metrics;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class MetricsStore {
    private final AtomicReference<ServerMetrics> latest = new AtomicReference<>();
    private final AtomicReference<MetricsStatus> status = new AtomicReference<>(MetricsStatus.CONNECTED);
    public Optional<ServerMetrics> latest() { return Optional.ofNullable(latest.get()); }
    public void publish(ServerMetrics metrics) { latest.set(metrics); status.set(MetricsStatus.SUCCESS); }
    public MetricsStatus status() { return status.get(); }
    public void status(MetricsStatus value) { status.set(value); }
    public void clear() { latest.set(null); status.set(MetricsStatus.CONNECTED); }
}
