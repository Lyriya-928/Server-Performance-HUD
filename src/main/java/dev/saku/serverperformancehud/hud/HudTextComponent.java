package dev.saku.serverperformancehud.hud;

import net.minecraft.network.chat.Component;

/** One metric represented by separately styled label and value components. */
public record HudTextComponent(Component label, Component value, int labelColor, int valueColor,
                               boolean bold, boolean italic, boolean shadow) {
    public Component labelStyled() { return styled(label, labelColor); }
    public Component valueStyled() { return styled(value, valueColor); }
    private Component styled(Component source, int color) {
        return source.copy().withStyle(style -> style.withColor(color).withBold(bold).withItalic(italic));
    }
}
