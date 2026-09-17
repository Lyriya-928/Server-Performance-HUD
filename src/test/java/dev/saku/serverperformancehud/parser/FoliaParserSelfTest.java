package dev.saku.serverperformancehud.parser;

import dev.saku.serverperformancehud.config.ThresholdConfig;
import dev.saku.serverperformancehud.config.HudSlotConfig;
import dev.saku.serverperformancehud.config.HudTextColor;
import dev.saku.serverperformancehud.config.HudTextConfig;
import dev.saku.serverperformancehud.config.HudField;
import dev.saku.serverperformancehud.config.HudTextStyle;
import dev.saku.serverperformancehud.config.MetricThresholdConfig;
import dev.saku.serverperformancehud.config.HudConfig;
import dev.saku.serverperformancehud.config.ServerMode;
import dev.saku.serverperformancehud.config.ConnectionType;
import dev.saku.serverperformancehud.config.ConnectionTypeResolver;
import dev.saku.serverperformancehud.hud.PerformanceStatusCalculator;
import dev.saku.serverperformancehud.metrics.PeriodMetrics;

public final class FoliaParserSelfTest {
    public static void main(String[] args) {
        FoliaTpsRegionParser parser = new FoliaTpsRegionParser();
        long now = System.currentTimeMillis();
        check(parser.accept(Message.of("Chunks: 1,449 Players: 4 Entities: 2,605", now)) instanceof ParseResult.InProgress);
        check(parser.accept(Message.of("5s: 12.4% util at 9.1 MSPT at 19.8 TPS", now)) instanceof ParseResult.InProgress);
        check(parser.accept(Message.of("15s: 19,9%util at 10,04MSPT at 20.00TPS", now)) instanceof ParseResult.InProgress);
        check(parser.accept(Message.of("1m: 18% util at 11 MSPT at 19.5 TPS", now)) instanceof ParseResult.InProgress);
        check(parser.accept(Message.of("5m: 17% util at 12 MSPT at 19 TPS", now)) instanceof ParseResult.InProgress);
        check(parser.accept(Message.of("15m: 16% util at 13 MSPT at 18.5 TPS", now)) instanceof ParseResult.InProgress);
        ParseResult result = parser.accept(Message.of("Region around block [w:'world_nether',-30000009,80,29999751]:", now));
        check(result instanceof ParseResult.Success);
        ParseResult.Success success = (ParseResult.Success) result;
        check(success.metrics().periods().containsKey("15s"));
        check(success.metrics().period("15s").mspt() == 10.04);
        check(success.metrics().period("15s").utilization() == 19.9);
        check(success.metrics().periods().size() == 5);
        check(success.metrics().source().name().equals("COMMAND"));
        parser.reset();
        check(new CommandSession(now).accepts(now));
        check(!new CommandSession(now).accepts(now - 1));
        check(FoliaTpsRegionParser.looksLikeResponse("Region around block [w:'world',1,64,2]:"));
        check(FoliaTpsRegionParser.looksLikeResponse("15s: 19,9%util at 10,04MSPT at 20.00TPS"));
        check(!FoliaTpsRegionParser.looksLikeResponse("Player chat: my TPS is 20"));
        ThresholdConfig thresholds = new ThresholdConfig();
        check(PerformanceStatusCalculator.calculate(new PeriodMetrics(10.0, 10.0, 20.0), thresholds).status()
                == PerformanceStatusCalculator.Status.NORMAL);
        check(PerformanceStatusCalculator.calculate(new PeriodMetrics(90.0, 30.0, 20.0), thresholds).status()
                == PerformanceStatusCalculator.Status.WARNING);
        check(PerformanceStatusCalculator.calculate(new PeriodMetrics(100.0, 60.0, 10.0), thresholds).status()
                == PerformanceStatusCalculator.Status.CRITICAL);
        check(PerformanceStatusCalculator.calculate(new PeriodMetrics(null, null, null), thresholds).status()
                == PerformanceStatusCalculator.Status.UNKNOWN);
        HudSlotConfig limited = new HudSlotConfig();
        limited.fields = java.util.EnumSet.allOf(HudField.class);
        limited.normalize(0);
        check(limited.fields.size() == HudSlotConfig.MAX_FIELDS);
        check(limited.fields.contains(HudField.TPS));
        HudTextConfig text = new HudTextConfig();
        text.color = HudTextColor.CUSTOM;
        text.customHex = "#55FFAA";
        check(text.resolveColor() == 0xFF55FFAA);
        MetricThresholdConfig metricThresholds = new MetricThresholdConfig();
        check(PerformanceStatusCalculator.metricStatus(HudField.TPS, 19.0, metricThresholds)
                == PerformanceStatusCalculator.Status.NORMAL);
        check(PerformanceStatusCalculator.metricStatus(HudField.MSPT, 30.0, metricThresholds)
                == PerformanceStatusCalculator.Status.WARNING);
        check(PerformanceStatusCalculator.metricStatus(HudField.PING, 150.0, metricThresholds)
                == PerformanceStatusCalculator.Status.CRITICAL);
        HudTextStyle fieldStyle = new HudTextStyle();
        fieldStyle.labelColor = "invalid";
        fieldStyle.normalize();
        check(fieldStyle.labelColor.equals("#B0B0B0"));
        HudConfig hudConfig = new HudConfig();
        hudConfig.normalize();
        check(hudConfig.serverMode == ServerMode.AUTO);
        check(ConnectionTypeResolver.resolve(true, true) == ConnectionType.INTEGRATED);
        check(ConnectionTypeResolver.resolve(false, false) == ConnectionType.REMOTE);
        check(ConnectionTypeResolver.resolve(false, true) == ConnectionType.REMOTE);
        check(parser.accept(Message.of("Unknown command", now)) instanceof ParseResult.Failure);
        System.out.println("FoliaParserSelfTest: OK");
    }
    private static void check(boolean value) { if (!value) throw new AssertionError(); }
}
