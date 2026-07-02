package com.patchnote.lanpause.dialog;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.patchnote.lanpause.LanPause;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Custom dialog body: a two-column vote "table". Each column has a header and a row of item icons
 * (player heads) for the players who chose that option. Serialized to the client via the dialog
 * registry-codec dispatch, then rendered by {@code VotesBodyHandler} (client). Vanilla dialog bodies
 * only stack vertically, so the side-by-side columns live in our own client widget.
 */
public record VotesBody(Component leftHeader, List<ItemStackTemplate> leftHeads,
                        Component rightHeader, List<ItemStackTemplate> rightHeads,
                        int cellSize) implements DialogBody
{
    public static final MapCodec<VotesBody> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ComponentSerialization.CODEC.fieldOf("left_header").forGetter(VotesBody::leftHeader),
        ItemStackTemplate.CODEC.listOf().fieldOf("left_heads").forGetter(VotesBody::leftHeads),
        ComponentSerialization.CODEC.fieldOf("right_header").forGetter(VotesBody::rightHeader),
        ItemStackTemplate.CODEC.listOf().fieldOf("right_heads").forGetter(VotesBody::rightHeads),
        Codec.INT.optionalFieldOf("cell_size", 20).forGetter(VotesBody::cellSize))
        .apply(i, VotesBody::new));

    /** Runs on both physical sides (common entrypoint) so the client can decode the body. */
    public static void register()
    {
        Registry.register(BuiltInRegistries.DIALOG_BODY_TYPE, LanPause.id("votes"), MAP_CODEC);
    }

    @Override
    public MapCodec<? extends DialogBody> mapCodec() { return MAP_CODEC; }
}
