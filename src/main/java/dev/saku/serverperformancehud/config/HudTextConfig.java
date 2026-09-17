package dev.saku.serverperformancehud.config;

public final class HudTextConfig {
    public HudTextColor color = HudTextColor.WHITE;
    public String customHex = "#FFFFFF";
    public boolean shadow = true;

    public int resolveColor() {
        if (color != HudTextColor.CUSTOM) return color.defaultColor;
        try { return 0xFF000000 | Integer.parseInt(customHex.replace("#", ""), 16); }
        catch (RuntimeException ignored) { return HudTextColor.WHITE.defaultColor; }
    }

    public void normalize() {
        if (color == null) color = HudTextColor.WHITE;
        if (customHex == null || !customHex.matches("#[0-9a-fA-F]{6}")) customHex = "#FFFFFF";
    }
}
