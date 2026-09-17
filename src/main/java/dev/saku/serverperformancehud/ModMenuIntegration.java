package dev.saku.serverperformancehud;

import dev.saku.serverperformancehud.config.HudConfigScreen;
import dev.saku.serverperformancehud.config.HudConfigStore;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new HudConfigScreen(parent, HudConfigStore.load());
    }
}

