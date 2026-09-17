package dev.saku.serverperformancehud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonObject;

public final class HudConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HudConfig current;
    private HudConfigStore() {}

    public static HudConfig load() {
        if (current != null) return current;
        Path path = FabricLoader.getInstance().getConfigDir().resolve("server-performance-hud.json");
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path, StandardCharsets.UTF_8);
                HudConfig config = GSON.fromJson(json, HudConfig.class);
                if (config != null) {
                    JsonObject root = GSON.fromJson(json, JsonObject.class);
                    if (!root.has("serverMode")) config.serverMode = root.has("dataSource")
                            ? (config.dataSource == dev.saku.serverperformancehud.metrics.DataSource.PREDICTION ? ServerMode.PREDICTION : ServerMode.COMMAND)
                            : ServerMode.AUTO;
                    config.normalize(); current = config; save(config); return config;
                }
            }
        } catch (IOException | RuntimeException ignored) { }
        HudConfig config = new HudConfig();
        config.normalize();
        current = config;
        save(config);
        return config;
    }

    public static void save(HudConfig config) {
        config.normalize();
        current = config;
        Path path = FabricLoader.getInstance().getConfigDir().resolve("server-performance-hud.json");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (IOException ignored) { }
    }
}
