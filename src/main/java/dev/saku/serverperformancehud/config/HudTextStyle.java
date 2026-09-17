package dev.saku.serverperformancehud.config;

public final class HudTextStyle {
    public String labelColor = "#B0B0B0";
    public ValueColorMode valueColorMode = ValueColorMode.PERFORMANCE;
    public String staticValueColor = "#FFFFFF";
    public String normalColor = "#55FF55";
    public String warningColor = "#FFAA00";
    public String criticalColor = "#FF5555";
    public boolean bold;
    public boolean italic;
    public boolean shadow = true;
    public String font = "minecraft:default";

    public void normalize() {
        if (valueColorMode == null) valueColorMode = ValueColorMode.PERFORMANCE;
        labelColor = valid(labelColor) ? labelColor : "#B0B0B0";
        staticValueColor = valid(staticValueColor) ? staticValueColor : "#FFFFFF";
        normalColor = valid(normalColor) ? normalColor : "#55FF55";
        warningColor = valid(warningColor) ? warningColor : "#FFAA00";
        criticalColor = valid(criticalColor) ? criticalColor : "#FF5555";
        if (font == null || font.isBlank()) font = "minecraft:default";
    }

    public static int color(String value, int fallback) {
        try { return 0xFF000000 | Integer.parseInt(value.replace("#", ""), 16); }
        catch (RuntimeException ignored) { return fallback; }
    }

    private static boolean valid(String value) { return value != null && value.matches("#[0-9a-fA-F]{6}"); }
}
