package dev.saku.serverperformancehud.config;

import dev.saku.serverperformancehud.metrics.DataSource;

public final class HudConfig {
    public boolean enabled = true;
    public DataSource dataSource = DataSource.COMMAND;
    public ServerMode serverMode = ServerMode.AUTO;
    public HudMode displayMode = HudMode.BOSSBAR;
    public String command = "/tps region";
    public int commandIntervalSeconds = 5;
    public int commandTimeoutSeconds = 3;
    public String period = "15s";
    public long staleTimeoutMillis = 12_000;
    public int spacing = 6;
    public int xOffset;
    public int yOffset;
    public HudTextConfig text = new HudTextConfig();
    public HudSlotConfig[] slots = createSlots();

    private static HudSlotConfig[] createSlots() {
        HudSlotConfig[] value = new HudSlotConfig[5];
        for (int i = 0; i < value.length; i++) {
            value[i] = new HudSlotConfig();
            value[i].enabled = i < 1;
        }
        return value;
    }

    public void normalize() {
        if (command == null || command.isBlank()) command = "/tps region";
        if (period == null || period.isBlank()) period = "15s";
        commandIntervalSeconds = Math.max(1, commandIntervalSeconds);
        commandTimeoutSeconds = Math.max(1, commandTimeoutSeconds);
        staleTimeoutMillis = Math.max(1_000, staleTimeoutMillis);
        if (dataSource == null) dataSource = DataSource.COMMAND;
        if (serverMode == null) serverMode = ServerMode.AUTO;
        if (displayMode == null) displayMode = HudMode.BOSSBAR;
        if (text == null) text = new HudTextConfig();
        text.normalize();
        if (slots == null || slots.length != 5) {
            HudSlotConfig[] old = slots;
            slots = createSlots();
            if (old != null) System.arraycopy(old, 0, slots, 0, Math.min(old.length, slots.length));
        }
        for (int i = 0; i < slots.length; i++) if (slots[i] == null) slots[i] = new HudSlotConfig();
        for (int i = 0; i < slots.length; i++) slots[i].normalize(i);
        spacing = Math.max(0, Math.min(32, spacing));
        xOffset = Math.max(-500, Math.min(500, xOffset));
        yOffset = Math.max(-500, Math.min(500, yOffset));
    }
}
