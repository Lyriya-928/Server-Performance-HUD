package dev.saku.serverperformancehud.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudConfigScreen extends Screen {
    private final Screen parent;
    private final HudConfig config;
    private EditBox intervalBox;
    private EditBox timeoutBox;

    public HudConfigScreen(Screen parent, HudConfig config) {
        super(Component.translatable("server_performance_hud.config.title"));
        this.parent = parent;
        this.config = config;
        config.normalize();
    }

    @Override protected void init() {
        int left = (width - 520) / 2;
        int top = Math.max(20, (height - 330) / 2);
        addRenderableWidget(Checkbox.builder(Component.translatable("server_performance_hud.config.enable"), font)
                .pos(left + 12, top + 28).selected(config.enabled).onValueChange((b, v) -> config.enabled = v).build());
        addRenderableWidget(CycleButton.builder(HudConfigScreen::modeText, config.serverMode)
                .withValues(ServerMode.values()).create(left + 250, top + 24, 210, 20,
                        Component.translatable("server_performance_hud.config.server_mode"), (b, v) -> config.serverMode = v));
        addRenderableWidget(CycleButton.builder(HudConfigScreen::displayText, config.displayMode)
                .withValues(HudMode.values()).create(left + 250, top + 63, 210, 20,
                        Component.translatable("server_performance_hud.config.display"), (b, v) -> config.displayMode = v));
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.color." + value.name().toLowerCase()), config.text.color)
                .withValues(HudTextColor.values()).create(left + 12, top + 63, 210, 20,
                        Component.translatable("server_performance_hud.config.text_color"), (b, v) -> config.text.color = v));
        intervalBox = new EditBox(font, left + 12, top + 101, 90, 20, Component.translatable("server_performance_hud.config.interval"));
        intervalBox.setValue(Integer.toString(config.commandIntervalSeconds));
        addRenderableWidget(intervalBox);
        timeoutBox = new EditBox(font, left + 125, top + 101, 90, 20, Component.translatable("server_performance_hud.config.timeout"));
        timeoutBox.setValue(Integer.toString(config.commandTimeoutSeconds));
        addRenderableWidget(timeoutBox);
        for (int i = 0; i < config.slots.length; i++) {
            int row = top + 145 + i * 29;
            int index = i;
            addRenderableWidget(Button.builder(Component.translatable(config.slots[i].enabled
                            ? "server_performance_hud.config.slot_enabled" : "server_performance_hud.config.slot_disabled", i + 1), b -> {
                        config.slots[index].enabled = !config.slots[index].enabled;
                        b.setMessage(Component.translatable(config.slots[index].enabled
                                ? "server_performance_hud.config.slot_enabled" : "server_performance_hud.config.slot_disabled", index + 1));
                    }).bounds(left + 12, row, 300, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.edit"),
                    b -> minecraft.gui.setScreen(new HudSlotScreen(this, config, index))).bounds(left + 330, row, 130, 20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.save"), b -> closeAndSave())
                .bounds(left + 150, height - 34, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.cancel"), b -> minecraft.gui.setScreen(parent))
                .bounds(left + 270, height - 34, 100, 20).build());
    }

    private static Component modeText(ServerMode mode) { return Component.translatable("server_performance_hud.mode." + mode.name().toLowerCase()); }
    private static Component displayText(HudMode mode) { return Component.translatable("server_performance_hud.display." + mode.name().toLowerCase()); }
    private void closeAndSave() {
        try { config.commandIntervalSeconds = Math.max(1, Integer.parseInt(intervalBox.getValue().trim())); } catch (NumberFormatException ignored) { }
        try { config.commandTimeoutSeconds = Math.max(1, Integer.parseInt(timeoutBox.getValue().trim())); } catch (NumberFormatException ignored) { }
        config.normalize();
        HudConfigStore.save(config);
        minecraft.gui.setScreen(parent);
    }
    @Override public void onClose() { closeAndSave(); }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int left = (width - 520) / 2;
        int top = Math.max(20, (height - 330) / 2);
        graphics.centeredText(font, title, width / 2, top, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.general"), left + 12, top + 12, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.display_section"), left + 12, top + 51, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.interval"), left + 12, top + 91, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.timeout"), left + 125, top + 91, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.slots"), left + 12, top + 132, 0xFFFFFFFF);
    }
}
