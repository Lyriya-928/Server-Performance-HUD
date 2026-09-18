package dev.saku.serverperformancehud;

import dev.saku.serverperformancehud.command.CommandMetricsProvider;
import dev.saku.serverperformancehud.config.HudConfig;
import dev.saku.serverperformancehud.config.HudConfigStore;
import dev.saku.serverperformancehud.config.DetectionState;
import dev.saku.serverperformancehud.config.ServerMode;
import dev.saku.serverperformancehud.config.ConnectionType;
import dev.saku.serverperformancehud.config.ConnectionTypeResolver;
import dev.saku.serverperformancehud.hud.HudRenderer;
import dev.saku.serverperformancehud.metrics.MetricsStore;
import dev.saku.serverperformancehud.prediction.PredictionMetricsProvider;
import dev.saku.serverperformancehud.local.LocalMetricsProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerPerformanceHudClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerPerformanceHud");
    private static MetricsStore store;
    private static PredictionMetricsProvider prediction;
    private static CommandMetricsProvider command;
    private static LocalMetricsProvider local;
    private static HudConfig config;
    private static long clientTicks;
    private static ServerMode activeMode;
    private static DetectionState detectionState = DetectionState.UNKNOWN;
    private static boolean probeStarted;
    private static ConnectionType connectionType = ConnectionType.NONE;

    @Override
    public void onInitializeClient() {
        config = HudConfigStore.load();
        store = new MetricsStore();
        prediction = new PredictionMetricsProvider(store);
        command = new CommandMetricsProvider(Minecraft.getInstance(), store);
        local = new LocalMetricsProvider(Minecraft.getInstance(), store);
        command.configure(config.command, config.commandIntervalSeconds, config.commandTimeoutSeconds);
        activeMode = config.serverMode;
        detectionState = activeMode == ServerMode.AUTO ? DetectionState.UNKNOWN
                : activeMode == ServerMode.COMMAND ? DetectionState.COMMAND : DetectionState.PREDICTION;
        HudRenderer renderer = new HudRenderer(store, config);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ConnectionType now = connectionType(client);
            if (client.player != null && now != connectionType) resetWorldMode(client, now);
            local.tick();
            if (connectionType == ConnectionType.REMOTE) command.tick(++clientTicks);
            if (connectionType == ConnectionType.REMOTE && activeMode == ServerMode.AUTO && !probeStarted && client.player != null) {
                probeStarted = true;
                detectionState = DetectionState.PROBING;
                LOGGER.info("[SPH] Connection type: REMOTE; capability state: UNKNOWN; starting /tps region capability probe");
                command.probe(success -> {
                    if (success) {
                        activeMode = ServerMode.COMMAND;
                        detectionState = DetectionState.COMMAND;
                        prediction.stop();
                        command.start();
                        LOGGER.info("[SPH] Capability state: COMMAND_SUPPORTED; metrics provider: COMMAND");
                    } else {
                        activeMode = ServerMode.PREDICTION;
                        detectionState = DetectionState.PREDICTION;
                        command.stop();
                        LOGGER.info("[SPH] Capability state: COMMAND_UNSUPPORTED; metrics provider: PREDICTION");
                    }
                });
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            probeStarted = false;
            connectionType = ConnectionType.NONE;
            command.stop();
            prediction.stop();
            local.stop();
            store.clear();
            activeMode = config.serverMode;
            detectionState = config.serverMode == ServerMode.AUTO ? DetectionState.UNKNOWN
                    : config.serverMode == ServerMode.COMMAND ? DetectionState.COMMAND : DetectionState.PREDICTION;
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("server-performance-hud", "hud"),
                (graphics, tickCounter) -> renderer.render(graphics));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("sph")
                        .then(ClientCommands.literal("test").executes(context -> {
                            testCommand();
                            return 1;
                        }))));
    }

    public static void onServerTimePacket() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        if (config == null || connectionType != ConnectionType.REMOTE || activeMode != ServerMode.PREDICTION) return;
        prediction.onServerTimePacket(System.nanoTime(), client.level.dimension().identifier().toString(),
                client.player.blockPosition().getX(), client.player.blockPosition().getY(), client.player.blockPosition().getZ());
    }

    public static boolean onSystemMessage(String message) {
        return command != null && command.acceptAutomatedMessage(message, System.currentTimeMillis());
    }

    public static void testCommand() {
        if (command != null && connectionType == ConnectionType.REMOTE) command.testCommand();
    }
    public static DetectionState detectionState() { return detectionState; }

    private static void resetWorldMode(Minecraft client, ConnectionType type) {
        connectionType = type;
        probeStarted = false;
        command.stop();
        prediction.stop();
        local.stop();
        if (type == ConnectionType.INTEGRATED) {
            local.start();
            detectionState = DetectionState.UNKNOWN;
            LOGGER.info("[SPH] Connection type: INTEGRATED; metrics provider: LOCAL; command polling disabled");
        } else if (config.serverMode == ServerMode.PREDICTION) {
            activeMode = ServerMode.PREDICTION;
            detectionState = DetectionState.PREDICTION;
            prediction.start();
        } else if (config.serverMode == ServerMode.COMMAND) {
            activeMode = ServerMode.COMMAND;
            detectionState = DetectionState.COMMAND;
            command.start();
            LOGGER.info("[SPH] Connection type: REMOTE; metrics provider: COMMAND");
        } else {
            activeMode = ServerMode.AUTO;
            detectionState = DetectionState.UNKNOWN;
            prediction.start();
            LOGGER.info("[SPH] Connection type: REMOTE; metrics provider: PREDICTION");
        }
    }

    private static ConnectionType connectionType(Minecraft client) {
        return ConnectionTypeResolver.resolve(client.hasSingleplayerServer(), client.getSingleplayerServer() != null);
    }
}
