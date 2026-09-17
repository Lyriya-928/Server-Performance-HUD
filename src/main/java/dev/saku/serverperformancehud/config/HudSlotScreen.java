package dev.saku.serverperformancehud.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;

public final class HudSlotScreen extends Screen {
    private final Screen parent;
    private final HudConfig config;
    private final HudSlotConfig slot;
    private static final HudField[] FIELDS = HudField.values();
    private final java.util.List<Button> fieldButtons = new java.util.ArrayList<>();
    private EditBox xOffsetBox;
    private EditBox yOffsetBox;

    public HudSlotScreen(Screen parent, HudConfig config, int index) {
        super(Component.translatable("server_performance_hud.config.slot_title", index + 1));
        this.parent = parent;
        this.config = config;
        this.slot = config.slots[index];
    }

    @Override
    protected void init() {
        int left = width / 2 - 180;
        addRenderableWidget(Checkbox.builder(Component.translatable("server_performance_hud.config.enable"), font).pos(left, 30)
                .selected(slot.enabled).onValueChange((box, value) -> slot.enabled = value).build());
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.style." + value.name().toLowerCase()), slot.style)
                .withValues(HudStyle.values()).create(left + 150, 26, 150, 20, Component.translatable("server_performance_hud.config.style"), (button, value) -> slot.style = value));
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.position." + value.name().toLowerCase()), slot.position)
                .withValues(HudPosition.values()).create(left, 57, 180, 20, Component.translatable("server_performance_hud.config.position"), (button, value) -> slot.position = value));
        addRenderableWidget(CycleButton.builder(Component::literal, slot.period)
                .withValues("5s", "15s", "1m", "5m", "15m").create(left + 190, 57, 110, 20, Component.translatable("server_performance_hud.config.period"), (button, value) -> slot.period = value));
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.bar_mode." + value.name().toLowerCase()), slot.bar.mode)
                .withValues(BarColorMode.values()).create(left, 82, 170, 20, Component.translatable("server_performance_hud.config.bar_mode"), (button, value) -> slot.bar.mode = value));
        addRenderableWidget(CycleButton.builder(value -> Component.translatable("server_performance_hud.color." + value.name().toLowerCase()), BossEvent.BossBarColor.valueOf(slot.bar.staticColor))
                .withValues(BossEvent.BossBarColor.values()).create(left + 180, 82, 150, 20, Component.translatable("server_performance_hud.config.bar_color"), (button, value) -> slot.bar.staticColor = value.name()));
        xOffsetBox = new EditBox(font, left, 108, 75, 20, Component.translatable("server_performance_hud.config.x_offset"));
        xOffsetBox.setValue(Integer.toString(slot.xOffset));
        addRenderableWidget(xOffsetBox);
        yOffsetBox = new EditBox(font, left + 85, 108, 75, 20, Component.translatable("server_performance_hud.config.y_offset"));
        yOffsetBox.setValue(Integer.toString(slot.yOffset));
        addRenderableWidget(yOffsetBox);
        for (int i = 0; i < FIELDS.length; i++) {
            int column = i % 2;
            int row = i / 2;
            HudField field = FIELDS[i];
            Button fieldButton = Button.builder(Component.translatable((slot.fields.contains(field) ? "server_performance_hud.config.field_selected" : "server_performance_hud.config.field_unselected"), fieldText(field)), button -> {
                if (slot.fields.contains(field)) slot.fields.remove(field); else if (slot.fields.size() < HudSlotConfig.MAX_FIELDS) slot.fields.add(field);
                button.setMessage(Component.translatable((slot.fields.contains(field) ? "server_performance_hud.config.field_selected" : "server_performance_hud.config.field_unselected"), fieldText(field)));
                refreshFieldButtons();
            }).bounds(left + column * 190, 136 + row * 24, 140, 20).build();
            fieldButtons.add(fieldButton);
            addRenderableWidget(fieldButton);
            addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.style_button"), button -> minecraft.setScreen(new HudFieldStyleScreen(this, slot, field)))
                    .bounds(left + column * 190 + 145, 136 + row * 24, 45, 20).build());
        }
        refreshFieldButtons();
        int bottom = 136 + ((FIELDS.length + 1) / 2) * 24 + 8;
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.save"), button -> saveAndClose()).bounds(left, bottom, 110, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("server_performance_hud.config.back"), button -> minecraft.setScreen(parent)).bounds(left + 120, bottom, 110, 20).build());
    }

    private void refreshFieldButtons() {
        for (int i = 0; i < fieldButtons.size(); i++) {
            HudField field = FIELDS[i];
            fieldButtons.get(i).active = slot.fields.contains(field) || slot.fields.size() < HudSlotConfig.MAX_FIELDS;
        }
    }

    private void saveAndClose() {
        try { slot.xOffset = Integer.parseInt(xOffsetBox.getValue().trim()); } catch (NumberFormatException ignored) { }
        try { slot.yOffset = Integer.parseInt(yOffsetBox.getValue().trim()); } catch (NumberFormatException ignored) { }
        config.normalize();
        HudConfigStore.save(config);
        minecraft.setScreen(parent);
    }
    @Override public void onClose() { saveAndClose(); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        extractMenuBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.x_offset"), width / 2 - 180, 112, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.y_offset"), width / 2 - 95, 112, 0xFFFFFFFF);
        graphics.text(font, Component.translatable("server_performance_hud.config.selected", slot.fields.size(), HudSlotConfig.MAX_FIELDS), width / 2 - 50, 130, 0xFFFFFFFF);
    }

    private static Component fieldText(HudField field) { return Component.translatable("server_performance_hud.metric." + field.name().toLowerCase()); }
}
