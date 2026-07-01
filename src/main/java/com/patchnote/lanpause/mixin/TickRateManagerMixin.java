package com.patchnote.lanpause.mixin;

import com.patchnote.lanpause.pause.LanPauseFreeze;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla exempts players (and their vehicles) from tick-freeze so they can walk around during
 * {@code /tick freeze}. While the mod's pause is active we remove that exemption so players are
 * frozen too. Gated on {@link LanPauseFreeze#active} so ordinary {@code /tick freeze} is untouched.
 */
@Mixin(TickRateManager.class)
public abstract class TickRateManagerMixin
{
    @Shadow protected boolean runGameElements;

    @Inject(method = "isEntityFrozen", at = @At("HEAD"), cancellable = true)
    private void lanpause$freezePlayers(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        if (LanPauseFreeze.active && !this.runGameElements)
        {
            cir.setReturnValue(true);
        }
    }
}
