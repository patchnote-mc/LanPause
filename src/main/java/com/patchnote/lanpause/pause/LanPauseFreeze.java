package com.patchnote.lanpause.pause;

/**
 * Gate for the {@code TickRateManager#isEntityFrozen} mixin. When {@code true} the mod's pause is
 * active and players (normally exempt from tick-freeze) are frozen too. Set server-side by
 * {@link PauseManager} and mirrored on remote clients via a freeze-sync packet.
 */
public final class LanPauseFreeze
{
    public static volatile boolean active = false;

    private LanPauseFreeze() {}
}
