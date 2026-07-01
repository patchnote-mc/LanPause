package com.patchnote.lanpause.client;

import com.patchnote.lanpause.LanPause;
import com.patchnote.lanpause.client.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

public class LanPauseClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        LanPause.LOGGER.info("LAN Pause initializing ...");

        AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
    }

    public static ModConfig getConfig()
    {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
}
