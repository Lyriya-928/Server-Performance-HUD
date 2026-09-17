package dev.saku.serverperformancehud.hud;

import dev.saku.serverperformancehud.config.ThresholdConfig;
import dev.saku.serverperformancehud.config.MetricThresholdConfig;
import dev.saku.serverperformancehud.config.HudField;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;

/** Converts available performance metrics into one slot state and one bar progress. */
public final class PerformanceStatusCalculator {
    public enum Status { NORMAL, WARNING, CRITICAL, UNKNOWN }

    public record Result(Status status, double progress) {}

    private PerformanceStatusCalculator() {}

    public static Status metricStatus(HudField field, Double value, MetricThresholdConfig thresholds) {
        if (value == null || !Double.isFinite(value)) return Status.UNKNOWN;
        return switch (field) {
            case TPS -> value < thresholds.criticalTps ? Status.CRITICAL : value < thresholds.warningTps ? Status.WARNING : Status.NORMAL;
            case MSPT -> value > thresholds.criticalMspt ? Status.CRITICAL : value > thresholds.warningMspt ? Status.WARNING : Status.NORMAL;
            case PING -> value > thresholds.criticalPing ? Status.CRITICAL : value > thresholds.warningPing ? Status.WARNING : Status.NORMAL;
            case UTILIZATION -> value >= thresholds.criticalUtilization ? Status.CRITICAL : value >= thresholds.warningUtilization ? Status.WARNING : Status.NORMAL;
            default -> Status.NORMAL;
        };
    }

    public static Result calculate(PeriodMetrics metrics, ThresholdConfig thresholds) {
        if (metrics == null) return new Result(Status.UNKNOWN, 0.0);
        boolean critical = false;
        boolean warning = false;
        if (metrics.tps() != null) {
            critical |= metrics.tps() < thresholds.criticalTps;
            warning |= metrics.tps() < thresholds.warningTps;
        }
        if (metrics.mspt() != null) {
            critical |= metrics.mspt() > thresholds.criticalMspt;
            warning |= metrics.mspt() > thresholds.warningMspt;
        }
        if (metrics.utilization() != null) {
            critical |= metrics.utilization() >= 100.0;
            warning |= metrics.utilization() >= 85.0;
        }
        double total = 0.0;
        int count = 0;
        if (metrics.tps() != null) { total += clamp(metrics.tps() / 20.0); count++; }
        if (metrics.mspt() != null) { total += clamp(1.0 - (metrics.mspt() / 100.0)); count++; }
        if (metrics.utilization() != null) { total += clamp(1.0 - metrics.utilization() / 100.0); count++; }
        double progress = count == 0 ? 0.0 : total / count;
        Status status = critical ? Status.CRITICAL : warning ? Status.WARNING : count == 0 ? Status.UNKNOWN : Status.NORMAL;
        return new Result(status, progress);
    }

    private static double clamp(double value) { return Math.max(0.0, Math.min(1.0, value)); }
}
