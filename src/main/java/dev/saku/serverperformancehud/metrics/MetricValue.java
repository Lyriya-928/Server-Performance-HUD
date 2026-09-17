package dev.saku.serverperformancehud.metrics;

public record MetricValue<T>(T value, MetricSource source) {}

