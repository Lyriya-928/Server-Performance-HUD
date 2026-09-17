package dev.saku.serverperformancehud.mixin;

import dev.saku.serverperformancehud.ServerPerformanceHudClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "handleSetTime", at = @At("TAIL"))
    private void serverPerformanceHud$time(ClientboundSetTimePacket packet, CallbackInfo callbackInfo) {
        ServerPerformanceHudClient.onServerTimePacket();
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true)
    private void serverPerformanceHud$systemChat(ClientboundSystemChatPacket packet, CallbackInfo callbackInfo) {
        if (ServerPerformanceHudClient.onSystemMessage(packet.content().getString())) callbackInfo.cancel();
    }
}
