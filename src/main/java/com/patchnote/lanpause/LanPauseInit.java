package com.patchnote.lanpause;

import com.patchnote.lanpause.config.ModConfig;
import com.patchnote.lanpause.dialog.VotesBody;
import com.patchnote.lanpause.net.LanPauseNet;
import com.patchnote.lanpause.net.LanPauseNet.RequestPausePayload;
import com.patchnote.lanpause.pause.PauseManager;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class LanPauseInit implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        LanPause.LOGGER.info("LAN Pause initializing (common) ...");

        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        LanPauseNet.registerTypes();
        VotesBody.register();

        ServerPlayNetworking.registerGlobalReceiver(RequestPausePayload.TYPE, (payload, context) -> {
            PauseManager mgr = PauseManager.get();
            if (mgr != null) mgr.requestPause(context.player());
        });

        ServerLifecycleEvents.SERVER_STARTED.register(PauseManager::bind);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PauseManager.unbind());

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PauseManager mgr = PauseManager.get();
            if (mgr != null) mgr.tick();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PauseManager mgr = PauseManager.get();
            if (mgr != null) mgr.onPlayerJoin(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PauseManager mgr = PauseManager.get();
            if (mgr != null) mgr.onPlayerLeave(handler.player);
        });
    }
}
