package dev.saku.serverperformancehud.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;
import dev.saku.serverperformancehud.config.BarColorMode;
import dev.saku.serverperformancehud.config.HudBarConfig;

/** Renders a slot with the same sprite layers used by Minecraft's BossHealthOverlay. */
public final class BossBarStyleRenderer {
    public static final int BAR_WIDTH = 182;
    public static final int BAR_HEIGHT = 5;
    private static final Identifier[] BACKGROUNDS = {
            sprite("boss_bar/pink_background"), sprite("boss_bar/blue_background"),
            sprite("boss_bar/red_background"), sprite("boss_bar/green_background"),
            sprite("boss_bar/yellow_background"), sprite("boss_bar/purple_background"),
            sprite("boss_bar/white_background")
    };
    private static final Identifier[] PROGRESS = {
            sprite("boss_bar/pink_progress"), sprite("boss_bar/blue_progress"),
            sprite("boss_bar/red_progress"), sprite("boss_bar/green_progress"),
            sprite("boss_bar/yellow_progress"), sprite("boss_bar/purple_progress"),
            sprite("boss_bar/white_progress")
    };
    private static final Identifier NOTCHED_BACKGROUND = sprite("boss_bar/notched_10_background");
    private static final Identifier NOTCHED_PROGRESS = sprite("boss_bar/notched_10_progress");

    public void render(GuiGraphicsExtractor graphics, int x, int y, PerformanceStatusCalculator.Result result, HudBarConfig bar) {
        BossEvent.BossBarColor color = switch (bar.mode) {
            case STATIC -> parseColor(bar.staticColor);
            case CUSTOM_PERFORMANCE -> parseColor(switch (result.status()) {
                case NORMAL -> bar.normalColor;
                case WARNING -> bar.warningColor;
                case CRITICAL -> bar.criticalColor;
                case UNKNOWN -> bar.staticColor;
            });
            case PERFORMANCE -> performanceColor(result.status());
        };
        int fillWidth = (int) Math.round(BAR_WIDTH * result.progress());
        blit(graphics, BACKGROUNDS[color.ordinal()], x, y, BAR_WIDTH);
        blit(graphics, NOTCHED_BACKGROUND, x, y, BAR_WIDTH);
        if (fillWidth > 0) {
            blit(graphics, PROGRESS[color.ordinal()], x, y, fillWidth);
            blit(graphics, NOTCHED_PROGRESS, x, y, fillWidth);
        }
    }

    private static BossEvent.BossBarColor performanceColor(PerformanceStatusCalculator.Status status) {
        return switch (status) {
            case CRITICAL -> BossEvent.BossBarColor.RED;
            case WARNING -> BossEvent.BossBarColor.YELLOW;
            case NORMAL -> BossEvent.BossBarColor.GREEN;
            case UNKNOWN -> BossEvent.BossBarColor.WHITE;
        };
    }

    private static BossEvent.BossBarColor parseColor(String value) {
        try { return BossEvent.BossBarColor.valueOf(value.toUpperCase(java.util.Locale.ROOT)); }
        catch (RuntimeException ignored) { return BossEvent.BossBarColor.GREEN; }
    }

    private static void blit(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width) {
        // This is the same cropped-sprite overload used by BossHealthOverlay.
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, BAR_WIDTH, BAR_HEIGHT, 0, 0, x, y, width, BAR_HEIGHT);
    }

    private static Identifier sprite(String path) { return Identifier.withDefaultNamespace(path); }
}
