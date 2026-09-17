package dev.saku.serverperformancehud.hud;

import dev.saku.serverperformancehud.config.HudConfig;
import dev.saku.serverperformancehud.config.HudField;
import dev.saku.serverperformancehud.config.HudPosition;
import dev.saku.serverperformancehud.config.HudSlotConfig;
import dev.saku.serverperformancehud.config.HudStyle;
import dev.saku.serverperformancehud.config.HudTextStyle;
import dev.saku.serverperformancehud.config.ValueColorMode;
import dev.saku.serverperformancehud.config.MetricThresholdConfig;
import dev.saku.serverperformancehud.metrics.DataSource;
import dev.saku.serverperformancehud.metrics.MetricsStore;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;
import dev.saku.serverperformancehud.metrics.ServerMetrics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private final BossBarStyleRenderer performanceBars = new BossBarStyleRenderer();
    private final HudTextRenderer textRenderer = new HudTextRenderer();
    private final HudLayoutManager layout = new HudLayoutManager();
    private final MetricsStore store;
    private final HudConfig config;

    public HudRenderer(MetricsStore store, HudConfig config) { this.store = store; this.config = config; }

    public void render(GuiGraphicsExtractor graphics) {
        if (!config.enabled) return;
        ServerMetrics metrics = store.latest().orElse(null);
        int top = 0, bottom = 0;
        for (HudSlotConfig slot : config.slots) {
            if (!slot.enabled) continue;
            boolean isBottom = slot.position == HudPosition.BOTTOM_LEFT || slot.position == HudPosition.BOTTOM_CENTER || slot.position == HudPosition.BOTTOM_RIGHT;
            drawSlot(graphics, slot, metrics, isBottom ? bottom++ : top++);
        }
    }

    private void drawSlot(GuiGraphicsExtractor graphics, HudSlotConfig slot, ServerMetrics metrics, int stack) {
        PeriodMetrics period = period(metrics, slot);
        List<HudTextComponent> components = fields(slot, metrics, period);
        if (components.isEmpty()) return;
        boolean drawBar = config.displayMode == dev.saku.serverperformancehud.config.HudMode.BOSSBAR
                && slot.style == HudStyle.PERFORMANCE_BARS;
        int contentHeight = 10 + (drawBar ? BossBarStyleRenderer.BAR_HEIGHT + 3 : 0);
        int x = layout.x(graphics, slot.position, config.xOffset + slot.xOffset);
        int y = layout.y(graphics, slot, stack, contentHeight, config.spacing, config.yOffset + slot.yOffset);
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale((float) slot.scale);
        textRenderer.render(graphics, components, 0, 0, BossBarStyleRenderer.BAR_WIDTH, Minecraft.getInstance().font);
        if (drawBar) {
            PerformanceStatusCalculator.Result result = PerformanceStatusCalculator.calculate(period, slot.thresholds);
            performanceBars.render(graphics, 0, 12, result, slot.bar);
        }
        graphics.pose().popMatrix();
    }

    private List<HudTextComponent> fields(HudSlotConfig slot, ServerMetrics metrics, PeriodMetrics period) {
        List<HudTextComponent> result = new ArrayList<>();
        boolean stale = metrics != null && System.currentTimeMillis() - metrics.timestamp() > config.staleTimeoutMillis;
        for (HudField field : slot.fields) {
            Double numeric = numeric(field, metrics, period, stale);
            String value = value(field, metrics, period, stale);
            if (value == null) continue;
            HudTextStyle style = slot.textStyles.getOrDefault(field, new HudTextStyle());
            style.normalize();
            int labelColor = HudTextStyle.color(style.labelColor, 0xFFB0B0B0);
            int valueColor = style.valueColorMode == ValueColorMode.PERFORMANCE
                    ? performanceValueColor(field, numeric, slot.metricThresholds, style)
                    : HudTextStyle.color(style.staticValueColor, 0xFFFFFFFF);
            result.add(new HudTextComponent(Component.translatable("server_performance_hud.metric." + fieldKey(field)),
                    Component.literal(value), labelColor, valueColor, style.bold, style.italic, style.shadow));
        }
        if (slot.showStale && stale) {
            result.add(new HudTextComponent(Component.translatable("server_performance_hud.metric.status"),
                    Component.translatable("server_performance_hud.status.stale"), 0xFFB0B0B0, 0xFFFFAA00, false, false, true));
        }
        return result;
    }

    private static int performanceValueColor(HudField field, Double value, MetricThresholdConfig thresholds, HudTextStyle style) {
        return switch (PerformanceStatusCalculator.metricStatus(field, value, thresholds)) {
            case CRITICAL -> HudTextStyle.color(style.criticalColor, 0xFFFF5555);
            case WARNING -> HudTextStyle.color(style.warningColor, 0xFFFFAA00);
            case NORMAL -> HudTextStyle.color(style.normalColor, 0xFF55FF55);
            case UNKNOWN -> 0xFFAAAAAA;
        };
    }

    private static String fieldKey(HudField field) {
        return switch (field) {
            case UTILIZATION -> "utilization";
            case COORDINATES -> "coordinates";
            case LAST_UPDATE -> "last_update";
            default -> field.name().toLowerCase(Locale.ROOT);
        };
    }

    private static Double numeric(HudField field, ServerMetrics metrics, PeriodMetrics period, boolean stale) {
        if (stale) return null;
        return switch (field) {
            case TPS -> period == null ? null : period.tps();
            case MSPT -> metrics == null || metrics.source() == DataSource.PREDICTION || period == null ? null : period.mspt();
            case UTILIZATION -> metrics == null || metrics.source() == DataSource.PREDICTION || period == null ? null : period.utilization();
            case PING -> (double) ping();
            default -> null;
        };
    }

    private static String value(HudField field, ServerMetrics metrics, PeriodMetrics period, boolean stale) {
        return switch (field) {
            case TPS -> number(stale || period == null ? null : period.tps());
            case MSPT -> metrics == null || metrics.source() == DataSource.PREDICTION ? null : number(stale || period == null ? null : period.mspt());
            case UTILIZATION -> metrics == null || metrics.source() == DataSource.PREDICTION ? null : percent(stale || period == null ? null : period.utilization());
            case PING -> ping() + "ms";
            case WORLD -> metrics == null ? "--" : metrics.world();
            case COORDINATES -> metrics == null ? "--" : metrics.x() + ", " + metrics.y() + ", " + metrics.z();
            case CHUNKS -> metrics == null || stale || metrics.chunks() == null ? "--" : Integer.toString(metrics.chunks());
            case PLAYERS -> metrics == null || stale || metrics.players() == null ? "--" : Integer.toString(metrics.players());
            case ENTITIES -> metrics == null || stale || metrics.entities() == null ? "--" : Integer.toString(metrics.entities());
            case SOURCE -> metrics == null ? "--" : Component.translatable(metrics.source() == DataSource.PREDICTION ? "server_performance_hud.status.estimated" : "server_performance_hud.status.server").getString();
            case LAST_UPDATE -> metrics == null ? "--" : age(metrics.timestamp());
        };
    }

    private static PeriodMetrics period(ServerMetrics metrics, HudSlotConfig slot) {
        if (metrics == null) return null;
        PeriodMetrics value = metrics.period(slot.period);
        return value == null && !metrics.periods().isEmpty() ? metrics.periods().values().iterator().next() : value;
    }
    private static String number(Double value) { return value == null ? "--" : String.format(Locale.ROOT, "%.2f", value); }
    private static String percent(Double value) { return value == null ? "--" : String.format(Locale.ROOT, "%.2f%%", value); }
    private static String age(long timestamp) { return String.format(Locale.ROOT, "%.1fs", Math.max(0, (System.currentTimeMillis() - timestamp) / 1000.0)); }
    private static int ping() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return 0;
        PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }
}
