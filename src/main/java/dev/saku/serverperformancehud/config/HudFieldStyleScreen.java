package dev.saku.serverperformancehud.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudFieldStyleScreen extends Screen {
    private final Screen parent;
    private final HudTextStyle style;
    private EditBox label, normal, warning, critical, staticColor;

    public HudFieldStyleScreen(Screen parent, HudSlotConfig slot, HudField field) {
        super(Component.translatable("server_performance_hud.config.field_style_title", Component.translatable("server_performance_hud.metric." + field.name().toLowerCase())));
        this.parent = parent;
        this.style = slot.textStyles.computeIfAbsent(field, ignored -> new HudTextStyle());
        style.normalize();
    }

    @Override protected void init() {
        int x = width / 2 - 120;
        label = box(x, 35, "server_performance_hud.config.label_hex", style.labelColor);
        staticColor = box(x, 62, "server_performance_hud.config.static_hex", style.staticValueColor);
        normal = box(x, 89, "server_performance_hud.config.normal_hex", style.normalColor);
        warning = box(x, 116, "server_performance_hud.config.warning_hex", style.warningColor);
        critical = box(x, 143, "server_performance_hud.config.critical_hex", style.criticalColor);
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.value_mode." + value.name().toLowerCase()), style.valueColorMode)
                .withValues(ValueColorMode.values()).create(x + 130, 35, 130, 20, Component.translatable("server_performance_hud.config.value_color"), (b, v) -> style.valueColorMode = v));
        addRenderableWidget(Checkbox.builder(Component.translatable("server_performance_hud.config.bold"), font).pos(x + 130, 62).selected(style.bold).onValueChange((b, v) -> style.bold = v).build());
        addRenderableWidget(Checkbox.builder(Component.translatable("server_performance_hud.config.italic"), font).pos(x + 130, 89).selected(style.italic).onValueChange((b, v) -> style.italic = v).build());
        addRenderableWidget(Checkbox.builder(Component.translatable("server_performance_hud.config.shadow"), font).pos(x + 130, 116).selected(style.shadow).onValueChange((b, v) -> style.shadow = v).build());
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.save"), b -> save()).bounds(x, 180, 110, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.back"), b -> minecraft.setScreen(parent)).bounds(x + 120, 180, 110, 20).build());
    }

    private EditBox box(int x, int y, String hintKey, String value) {
        EditBox box = new EditBox(font, x, y, 115, 20, Component.translatable(hintKey));
        box.setValue(value); box.setMaxLength(7); addRenderableWidget(box); return box;
    }
    private void save() {
        style.labelColor = label.getValue(); style.staticValueColor = staticColor.getValue();
        style.normalColor = normal.getValue(); style.warningColor = warning.getValue(); style.criticalColor = critical.getValue();
        style.normalize(); minecraft.setScreen(parent);
    }
    @Override public void onClose() { save(); }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractMenuBackground(graphics); super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFFFFF);
    }
}
