package com.patchnote.lanpause.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.patchnote.lanpause.LanPause;
import com.patchnote.lanpause.net.LanPauseNet.FreezeSyncPayload;
import com.patchnote.lanpause.net.LanPauseNet.RequestPausePayload;
import com.patchnote.lanpause.pause.LanPauseFreeze;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

public class LanPauseClient implements ClientModInitializer
{
    private static KeyMapping pauseKey;

    @Override
    public void onInitializeClient()
    {
        LanPause.LOGGER.info("LAN Pause initializing (client) ...");

        pauseKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.lanpause.pause",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_SEMICOLON,
            KeyMapping.Category.MULTIPLAYER));

        ClientPlayNetworking.registerGlobalReceiver(FreezeSyncPayload.TYPE,
            (payload, context) -> LanPauseFreeze.active = payload.active());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (pauseKey.consumeClick())
            {
                if (client.player != null && ClientPlayNetworking.canSend(RequestPausePayload.TYPE))
                {
                    ClientPlayNetworking.send(new RequestPausePayload());
                }
            }
        });
    }
}
