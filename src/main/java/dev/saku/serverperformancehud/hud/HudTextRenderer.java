package dev.saku.serverperformancehud.hud;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Draws separately styled label/value components around the BossBar center. */
public final class HudTextRenderer {
    private final HudTextLayout layout = new HudTextLayout();

    public void render(GuiGraphicsExtractor graphics, List<HudTextComponent> components,
                       int barX, int y, int barWidth, Font font) {
        HudTextLayout.Measured measured = layout.measure(font, components);
        int cursor = barX + (barWidth - measured.width()) / 2;
        for (HudTextComponent component : measured.components()) {
            Component label = component.labelStyled();
            Component separator = Component.literal(": ");
            Component value = component.valueStyled();
            graphics.text(font, label, cursor, y, component.labelColor(), component.shadow());
            cursor += font.width(label);
            graphics.text(font, separator, cursor, y, component.labelColor(), component.shadow());
            cursor += font.width(separator);
            graphics.text(font, value, cursor, y, component.valueColor(), component.shadow());
            cursor += font.width(value) + HudTextLayout.SPACING;
        }
    }
}
