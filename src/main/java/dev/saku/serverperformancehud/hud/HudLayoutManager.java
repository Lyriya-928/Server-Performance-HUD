package dev.saku.serverperformancehud.hud;

import dev.saku.serverperformancehud.config.HudPosition;
import dev.saku.serverperformancehud.config.HudSlotConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;

import java.util.Map;
import java.util.UUID;

/** Owns slot stacking and top-area collision avoidance. */
public final class HudLayoutManager {
    private static final int VANILLA_BOSSBAR_START = 12;
    private static final int VANILLA_BOSSBAR_STEP = 19;

    public int x(GuiGraphicsExtractor graphics, HudPosition position, int offset) {
        int width = graphics.guiWidth();
        return switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> 8 + offset;
            case TOP_RIGHT, BOTTOM_RIGHT -> width - BossBarStyleRenderer.BAR_WIDTH - 8 + offset;
            default -> (width - BossBarStyleRenderer.BAR_WIDTH) / 2 + offset;
        };
    }

    public int y(GuiGraphicsExtractor graphics, HudSlotConfig slot, int stack, int contentHeight, int spacing, int offset) {
        boolean bottom = slot.position == HudPosition.BOTTOM_LEFT || slot.position == HudPosition.BOTTOM_CENTER || slot.position == HudPosition.BOTTOM_RIGHT;
        if (bottom) return graphics.guiHeight() - 8 - (stack + 1) * contentHeight - stack * spacing + offset;
        return vanillaBossBarHeight() + spacing + stack * (contentHeight + spacing) + offset;
    }

    public int vanillaBossBarHeight() {
        Minecraft client = Minecraft.getInstance();
        if (client.gui == null) return VANILLA_BOSSBAR_START;
        BossHealthOverlay overlay = client.gui.hud.getBossOverlay();
        if (!(overlay instanceof dev.saku.serverperformancehud.mixin.BossHealthOverlayAccessor accessor)) return VANILLA_BOSSBAR_START;
        Map<UUID, LerpingBossEvent> events = accessor.serverPerformanceHud$getEvents();
        return VANILLA_BOSSBAR_START + events.size() * VANILLA_BOSSBAR_STEP;
    }
}
