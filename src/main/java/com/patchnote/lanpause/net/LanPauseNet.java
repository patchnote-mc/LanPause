package com.patchnote.lanpause.net;

import com.patchnote.lanpause.LanPause;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class LanPauseNet
{
    // Identifier + payload-tag key used by the dialog vote buttons (CustomAll action).
    public static final Identifier VOTE_ID = LanPause.id("vote");
    public static final String CHOICE_KEY = "choice";
    public static final String CHOICE_PAUSE = "pause";
    public static final String CHOICE_RESUME = "resume";

    /** C2S: a client pressed the pause keybind and asks the server to start a pause vote. */
    public record RequestPausePayload() implements CustomPacketPayload
    {
        public static final Type<RequestPausePayload> TYPE = new Type<>(LanPause.id("request_pause"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestPausePayload> CODEC =
            StreamCodec.unit(new RequestPausePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    /** S2C: tells clients whether the mod's pause is active, so they set the local freeze flag. */
    public record FreezeSyncPayload(boolean active) implements CustomPacketPayload
    {
        public static final Type<FreezeSyncPayload> TYPE = new Type<>(LanPause.id("freeze_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FreezeSyncPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, FreezeSyncPayload::active, FreezeSyncPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void registerTypes()
    {
        PayloadTypeRegistry.serverboundPlay().register(RequestPausePayload.TYPE, RequestPausePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FreezeSyncPayload.TYPE, FreezeSyncPayload.CODEC);
    }

    private LanPauseNet() {}
}
