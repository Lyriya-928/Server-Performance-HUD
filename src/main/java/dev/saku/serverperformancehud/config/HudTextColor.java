package dev.saku.serverperformancehud.config;

public enum HudTextColor {
    WHITE(0xFFFFFFFF), GRAY(0xFFAAAAAA), GREEN(0xFF55FF55), YELLOW(0xFFFFFF55), RED(0xFFFF5555), BLUE(0xFF5555FF), CUSTOM(0xFFFFFFFF);

    public final int defaultColor;
    HudTextColor(int defaultColor) { this.defaultColor = defaultColor; }
}
