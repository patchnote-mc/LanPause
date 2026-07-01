package com.patchnote.lanpause.client.config;

import com.patchnote.lanpause.LanPause;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = LanPause.MOD_ID)
public final class ModConfig implements ConfigData
{
    public static ModConfig get() { return AutoConfig.getConfigHolder(ModConfig.class).getConfig(); }
}
