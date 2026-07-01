package com.patchnote.lanpause.mixin;

import com.patchnote.lanpause.net.LanPauseNet;
import com.patchnote.lanpause.pause.PauseManager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric has no event for dialog custom-click actions, so we tap the vanilla handler. The injection
 * point sits just after the thread-check, so this runs on the server thread. We route our own vote
 * button clicks to {@link PauseManager} and cancel the vanilla (debug-log-only) handling.
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCustomClickActionMixin
{
    @Inject(
        method = "handleCustomClickAction",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/resources/Identifier;Ljava/util/Optional;)V"),
        cancellable = true)
    private void lanpause$onVote(ServerboundCustomClickActionPacket packet, CallbackInfo ci)
    {
        if (!LanPauseNet.VOTE_ID.equals(packet.id())) return;
        if (!((Object) this instanceof ServerGamePacketListenerImpl listener)) return;

        PauseManager mgr = PauseManager.get();
        if (mgr == null) return;

        String choice = packet.payload()
            .filter(t -> t instanceof CompoundTag)
            .map(t -> ((CompoundTag) t).getStringOr(LanPauseNet.CHOICE_KEY, ""))
            .orElse("");
        mgr.onVote(listener.player, choice);
        ci.cancel();
    }
}
