package dev.saku.serverperformancehud.config;

public final class HudBarConfig {
    public BarColorMode mode = BarColorMode.PERFORMANCE;
    public String staticColor = "GREEN";
    public String normalColor = "GREEN";
    public String warningColor = "YELLOW";
    public String criticalColor = "RED";

    public void normalize() {
        if (mode == null) mode = BarColorMode.PERFORMANCE;
        staticColor = valid(staticColor, "GREEN");
        normalColor = valid(normalColor, "GREEN");
        warningColor = valid(warningColor, "YELLOW");
        criticalColor = valid(criticalColor, "RED");
    }

    private static String valid(String value, String fallback) {
        try { return net.minecraft.world.BossEvent.BossBarColor.valueOf(value.toUpperCase(java.util.Locale.ROOT)).name(); }
        catch (RuntimeException ignored) { return fallback; }
    }
}
