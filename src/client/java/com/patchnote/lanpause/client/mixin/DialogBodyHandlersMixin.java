package com.patchnote.lanpause.client.mixin;

import java.util.Map;

import com.mojang.serialization.MapCodec;
import com.patchnote.lanpause.client.dialog.VotesBodyHandler;
import com.patchnote.lanpause.dialog.VotesBody;

import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandler;
import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandlers;
import net.minecraft.server.dialog.body.DialogBody;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla has no API to register a client renderer for a custom dialog body, so we tap the private
 * handler map at the tail of {@code bootstrap()} and add ours for {@link VotesBody}.
 */
@Mixin(DialogBodyHandlers.class)
public abstract class DialogBodyHandlersMixin
{
    @Shadow @Final private static Map<MapCodec<? extends DialogBody>, DialogBodyHandler<?>> HANDLERS;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void lanpause$registerVotesBody(CallbackInfo ci)
    {
        HANDLERS.put(VotesBody.MAP_CODEC, new VotesBodyHandler());
    }
}
