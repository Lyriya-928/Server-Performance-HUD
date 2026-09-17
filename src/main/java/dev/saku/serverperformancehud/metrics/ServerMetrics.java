package dev.saku.serverperformancehud.metrics;

import java.util.Map;

public record ServerMetrics(DataSource source, String world, int x, int y, int z,
                            Map<String, PeriodMetrics> periods, Integer chunks,
                            Integer players, Integer entities, long timestamp) {
    public ServerMetrics { periods = Map.copyOf(periods); }
    public PeriodMetrics period(String name) { return periods.get(name); }
}

