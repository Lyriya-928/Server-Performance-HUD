package dev.saku.serverperformancehud.config;

/** Pure connection classification used after querying the Minecraft client runtime. */
public final class ConnectionTypeResolver {
    private ConnectionTypeResolver() { }

    public static ConnectionType resolve(boolean singleplayer, boolean integratedServerPresent) {
        return singleplayer && integratedServerPresent ? ConnectionType.INTEGRATED : ConnectionType.REMOTE;
    }
}
