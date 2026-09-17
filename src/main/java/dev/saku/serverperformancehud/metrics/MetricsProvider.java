package dev.saku.serverperformancehud.metrics;

import java.util.Optional;

public interface MetricsProvider {
    void start();
    void stop();
    Optional<ServerMetrics> getLatestMetrics();
}

