package dev.saku.serverperformancehud.config;

import java.util.EnumSet;
import java.util.Set;
import java.util.EnumMap;
import java.util.Map;

public final class HudSlotConfig {
    public static final int MAX_FIELDS = 4;
    public boolean enabled;
    public HudStyle style = HudStyle.PERFORMANCE_BARS;
    public HudPosition position = HudPosition.TOP_CENTER;
    public double scale = 1.0;
    public int xOffset;
    public int yOffset;
    public String period = "15s";
    public boolean showSource = true;
    public boolean showStale = true;
    public Set<HudField> fields = EnumSet.of(HudField.TPS, HudField.MSPT);
    public ThresholdConfig thresholds = new ThresholdConfig();
    public MetricThresholdConfig metricThresholds = new MetricThresholdConfig();
    public HudBarConfig bar = new HudBarConfig();
    public Map<HudField, HudTextStyle> textStyles = new EnumMap<>(HudField.class);

    public void normalize(int index) {
        if (style == null) style = HudStyle.PERFORMANCE_BARS;
        if (position == null) position = HudPosition.TOP_CENTER;
        if (period == null || period.isBlank()) period = "15s";
        if (scale <= 0 || !Double.isFinite(scale)) scale = 1.0;
        scale = Math.min(3.0, scale);
        xOffset = Math.max(-500, Math.min(500, xOffset));
        yOffset = Math.max(-500, Math.min(500, yOffset));
        if (fields == null) fields = EnumSet.of(HudField.TPS, HudField.MSPT);
        if (fields.size() > MAX_FIELDS) fields = fields.stream().limit(MAX_FIELDS)
                .collect(() -> EnumSet.noneOf(HudField.class), EnumSet::add, EnumSet::addAll);
        if (thresholds == null) thresholds = new ThresholdConfig();
        if (metricThresholds == null) metricThresholds = new MetricThresholdConfig();
        if (bar == null) bar = new HudBarConfig();
        bar.normalize();
        if (textStyles == null) textStyles = new EnumMap<>(HudField.class);
        for (HudField field : fields) {
            HudTextStyle styleForField = textStyles.computeIfAbsent(field, ignored -> new HudTextStyle());
            styleForField.normalize();
        }
    }
}
