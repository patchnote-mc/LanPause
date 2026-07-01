package com.patchnote.lanpause.config;

import com.patchnote.lanpause.LanPause;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = LanPause.MOD_ID)
public final class ModConfig implements ConfigData
{
    // Vote timer scales with player count: clamp(min + (players-1)*extra, min, max), in seconds.
    public int minVoteSeconds = 10;
    public int maxVoteSeconds = 30;
    public int extraSecondsPerPlayer = 5;

    // Fraction of online players that must agree to (un)pause. 0.5 = half.
    public double voteThreshold = 0.5;

    // The player who pressed the keybind is auto-counted as a "pause" vote.
    public boolean initiatorAutoVotes = true;

    // When the initial vote timer expires the side with more votes wins;
    // this only decides an exact tie (including no votes at all).
    public TieBreak onTimerTie = TieBreak.RESUME;

    public static ModConfig get() { return AutoConfig.getConfigHolder(ModConfig.class).getConfig(); }

    public enum TieBreak { PAUSE, RESUME }
}
