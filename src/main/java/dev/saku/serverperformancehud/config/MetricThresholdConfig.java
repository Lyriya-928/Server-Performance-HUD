package dev.saku.serverperformancehud.config;

public final class MetricThresholdConfig {
    public double warningTps = 18.0;
    public double criticalTps = 15.0;
    public double warningMspt = 25.0;
    public double criticalMspt = 50.0;
    public double warningPing = 50.0;
    public double criticalPing = 100.0;
    public double warningUtilization = 85.0;
    public double criticalUtilization = 100.0;
}
