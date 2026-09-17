package dev.saku.serverperformancehud.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Measures final Minecraft Components and calculates one centered text group. */
public final class HudTextLayout {
    public static final int SPACING = 8;
    public record Measured(List<HudTextComponent> components, int width) {}

    public Measured measure(Font font, List<HudTextComponent> components) {
        int width = 0;
        for (HudTextComponent component : components) {
            width += font.width(component.labelStyled()) + font.width(Component.literal(": "))
                    + font.width(component.valueStyled()) + SPACING;
        }
        return new Measured(components, Math.max(0, width - (components.isEmpty() ? 0 : SPACING)));
    }
}
